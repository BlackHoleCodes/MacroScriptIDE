package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.build.BuildResult;
import com.blackholecodes.macroscript.build.BuildService;
import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.ide.editor.CompletionSetup;
import com.blackholecodes.macroscript.ide.editor.EditorTheme;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.MappingLoader;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * What the IDE does: projects, files, building and decompiling. The window parts call this
 * class, and the menus are built from its methods by {@link MenuBarBuilder}.
 */
final class IdeController implements ProjectView.Listener, EditorServices {

    private static final String REFERENCE_RESOURCE = "/com/blackholecodes/macroscript/MacroScript_Reference.html";
    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 40;

    private final IdeSettings settings;
    private final BuildService builds = new BuildService();
    private final Mapping mapping = MappingLoader.loadDefault();
    private final CompletionProvider completions = CompletionSetup.createProvider(mapping);

    private final ProjectView projectView = new ProjectView(this);
    private final EditorTabs tabs = new EditorTabs(this, this::confirmCloseDirty);
    private final ProblemsPanel problems = new ProblemsPanel(this::navigateTo);
    private final BuildOutputPanel buildOutput = new BuildOutputPanel();
    private final StatusBar statusBar = new StatusBar();
    private final MainWindow window = new MainWindow(projectView, tabs, problems, buildOutput, statusBar);
    private final FindReplaceDialog findReplace = new FindReplaceDialog(window, this::currentTextArea);

    private Project project;

    IdeController(IdeSettings settings) {
        this.settings = settings;
        window.setJMenuBar(new MenuBarBuilder(this).build());
        settings.windowBounds().ifPresent(window::setBounds);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                projectView.refresh();
            }
        });
        tabs.addChangeListener(e -> currentTabChanged());
    }

    void start(Optional<Path> projectFolder) {
        window.showEditors(false);
        window.setVisible(true);
        projectFolder.ifPresent(this::openProject);
    }

    // ----- projects ----------------------------------------------------------------------

    void newProject() {
        chooseFolder("New Project: choose or create an empty folder").ifPresent(folder -> {
            try {
                Files.createDirectories(folder);
                boolean empty;
                try (Stream<Path> entries = Files.list(folder)) {
                    empty = entries.findAny().isEmpty();
                }
                openProject(folder);
                if (empty) {
                    createFile(folder, "My Macro", false);
                }
            } catch (IOException e) {
                showError("The project could not be created", e);
            }
        });
    }

    void openProjectDialog() {
        chooseFolder("Open Project").ifPresent(this::openProject);
    }

    void openProject(Path folder) {
        if (!closeProject()) {
            return;
        }
        try {
            project = Project.open(folder);
        } catch (IOException e) {
            showError("The project could not be opened", e);
            return;
        }
        settings.addRecentProject(project.root());
        projectView.setProject(project);
        window.setTitle(project.name() + " - MacroScript IDE");
        statusBar.setMessage(project.root().toString());
        for (Path file : project.openFiles()) {
            openFile(file);
        }
    }

    /** Closes the open tabs of the current project. Returns false when the user cancelled. */
    boolean closeProject() {
        if (project == null) {
            return tabs.closeAll();
        }
        project.setOpenFiles(tabs.openFiles());
        try {
            project.save();
        } catch (IOException e) {
            // The list of open files is a convenience; losing it is not worth stopping for.
            buildOutput.println("Could not save the project settings: " + e.getMessage());
        }
        if (!tabs.closeAll()) {
            return false;
        }
        project = null;
        projectView.setProject(null);
        window.setTitle("MacroScript IDE");
        return true;
    }

    List<Path> recentProjects() {
        return settings.recentProjects();
    }

    // ----- files -------------------------------------------------------------------------

    @Override
    public void openFile(Path file) {
        try {
            tabs.open(file);
        } catch (IOException e) {
            showError("The file could not be opened", e);
        }
    }

    void openFileDialog() {
        JFileChooser chooser = fileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("MacroScript files (*.ms)", "ms"));
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile().toPath());
        }
    }

    void newFileDialog(boolean actionBlock) {
        Optional<Path> folder = projectView.selectedFolder();
        if (folder.isEmpty()) {
            tabs.openUntitled(actionBlock ? FileTemplates.actionBlock("My Block") : FileTemplates.macro("My Macro"));
            return;
        }
        newFile(folder.get(), actionBlock);
    }

    @Override
    public void newFile(Path folder, boolean actionBlock) {
        String kind = actionBlock ? "action block" : "macro";
        String name = JOptionPane.showInputDialog(window, "Name of the new " + kind + ":", actionBlock ? "My Block" : "My Macro");
        if (name != null && !name.isBlank()) {
            createFile(folder, name.strip(), actionBlock);
        }
    }

    private void createFile(Path folder, String name, boolean actionBlock) {
        Path file = folder.resolve(name.replaceAll("[\\\\/:*?\"<>|]", "_") + BuildService.SOURCE_EXTENSION);
        if (Files.exists(file)) {
            showMessage("A file named " + file.getFileName() + " already exists.");
            return;
        }
        try {
            Files.writeString(file, actionBlock ? FileTemplates.actionBlock(name) : FileTemplates.macro(name));
            projectView.refresh();
            openFile(file);
        } catch (IOException e) {
            showError("The file could not be created", e);
        }
    }

    @Override
    public void newFolder(Path parent) {
        String name = JOptionPane.showInputDialog(window, "Name of the new folder:", "folder");
        if (name == null || name.isBlank()) {
            return;
        }
        try {
            Files.createDirectories(parent.resolve(name.strip()));
            projectView.refresh();
        } catch (IOException e) {
            showError("The folder could not be created", e);
        }
    }

    @Override
    public void rename(Path path) {
        String name = (String) JOptionPane.showInputDialog(window, "New name:", "Rename",
                JOptionPane.PLAIN_MESSAGE, null, null, path.getFileName().toString());
        if (name == null || name.isBlank() || name.equals(path.getFileName().toString())) {
            return;
        }
        Path target = path.resolveSibling(name.strip());
        try {
            Files.move(path, target);
        } catch (IOException e) {
            showError("The file could not be renamed", e);
            return;
        }
        for (EditorTab tab : tabs.tabs()) {
            if (tab.file() != null && tab.file().startsWith(path)) {
                tab.fileMoved(target.resolve(path.relativize(tab.file())));
            }
        }
        projectView.refresh();
    }

    @Override
    public void delete(Path path) {
        int answer = JOptionPane.showConfirmDialog(window, "Delete " + path.getFileName() + "?", "Delete",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path item : walk.sorted(Comparator.reverseOrder()).toList()) {
                tabs.discard(item);
                Files.delete(item);
            }
        } catch (IOException | UncheckedIOException e) {
            showError("The file could not be deleted", e);
        }
        projectView.refresh();
        currentTabChanged();
    }

    @Override
    public void reveal(Path path) {
        Path folder = Files.isDirectory(path) ? path : path.getParent();
        try {
            Desktop.getDesktop().open(folder.toFile());
        } catch (IOException | UnsupportedOperationException e) {
            showMessage("The folder is " + folder);
        }
    }

    void save() {
        tabs.current().ifPresent(this::save);
    }

    /** Saves a tab. Returns false when the file was not saved. */
    private boolean save(EditorTab tab) {
        if (tab.file() == null) {
            return saveAs(tab);
        }
        try {
            tab.save();
            return true;
        } catch (IOException e) {
            showError("The file could not be saved", e);
            return false;
        }
    }

    void saveAs() {
        tabs.current().ifPresent(this::saveAs);
    }

    private boolean saveAs(EditorTab tab) {
        JFileChooser chooser = fileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("MacroScript files (*.ms)", "ms"));
        if (chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().endsWith(BuildService.SOURCE_EXTENSION)) {
            file = file.resolveSibling(file.getFileName() + BuildService.SOURCE_EXTENSION);
        }
        try {
            tab.saveAs(file);
            projectView.refresh();
            return true;
        } catch (IOException e) {
            showError("The file could not be saved", e);
            return false;
        }
    }

    /** Saves every open file. Returns false when one could not be saved. */
    boolean saveAll() {
        for (EditorTab tab : tabs.tabs()) {
            if (tab.isDirty() && !save(tab)) {
                return false;
            }
        }
        return true;
    }

    void closeTab() {
        tabs.current().ifPresent(tabs::close);
    }

    private boolean confirmCloseDirty(EditorTab tab) {
        int answer = JOptionPane.showConfirmDialog(window, tab.title().substring(1) + " has changes. Save them?",
                "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return switch (answer) {
            case JOptionPane.YES_OPTION -> save(tab);
            case JOptionPane.NO_OPTION -> true;
            default -> false;
        };
    }

    void exit() {
        if (!closeProject()) {
            return;
        }
        settings.setWindowBounds(window.getBounds());
        window.dispose();
        System.exit(0);
    }

    // ----- building ----------------------------------------------------------------------

    /** Compiles the current file and writes its JSON to the output folder. */
    void buildCurrentFile() {
        Optional<EditorTab> current = tabs.current();
        if (current.isEmpty()) {
            showMessage("Open a .ms file to build it.");
            return;
        }
        if (!save(current.get())) {
            return;
        }
        build(List.of(current.get().file()));
    }

    /** Compiles every macro of the project. Action block files are built into the macros that call them. */
    void buildProject() {
        if (project == null) {
            buildCurrentFile();
            return;
        }
        if (!saveAll()) {
            return;
        }
        try {
            build(builds.macroFiles(project.root()));
        } catch (IOException e) {
            showError("The project files could not be read", e);
        }
    }

    private void build(List<Path> files) {
        buildOutput.clear();
        window.showBuildOutput();
        Path root = project != null ? project.root() : files.get(0).getParent();
        Path outputFolder = project != null ? project.outputFolder() : root.resolve("out");
        buildOutput.println("Building " + files.size() + (files.size() == 1 ? " file" : " files") + "...");
        new SwingWorker<List<BuildResult>, Void>() {
            @Override
            protected List<BuildResult> doInBackground() throws IOException {
                List<BuildResult> results = new ArrayList<>();
                for (Path file : files) {
                    results.add(builds.build(file, root, outputFolder, path -> Optional.empty()));
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    reportBuild(get());
                } catch (Exception e) {
                    buildOutput.println("Build failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void reportBuild(List<BuildResult> results) {
        int failed = 0;
        for (BuildResult result : results) {
            for (Diagnostic diagnostic : result.compile().diagnostics()) {
                buildOutput.println(diagnostic.inFile(result.source()).format());
            }
            if (result.succeeded()) {
                buildOutput.println("OK  " + result.source().getFileName() + "  ->  " + result.outputFile());
            } else {
                failed++;
                buildOutput.println("FAILED  " + result.source().getFileName());
            }
        }
        String summary = failed == 0
                ? "Build finished: " + results.size() + " compiled. Import the .json files in MacroDroid."
                : "Build finished with errors: " + failed + " of " + results.size() + " failed.";
        buildOutput.println(summary);
        statusBar.setMessage(summary);
        if (failed > 0) {
            window.showProblems();
        }
    }

    void openOutputFolder() {
        if (project == null) {
            showMessage("Open a project first.");
            return;
        }
        try {
            Files.createDirectories(project.outputFolder());
        } catch (IOException e) {
            showError("The output folder could not be created", e);
            return;
        }
        reveal(project.outputFolder());
    }

    // ----- decompiling -------------------------------------------------------------------

    void decompileDialog() {
        JFileChooser chooser = fileChooser();
        chooser.setDialogTitle("Decompile a MacroDroid export");
        chooser.setFileFilter(new FileNameExtensionFilter("MacroDroid exports (*.macro, *.json)", "macro", "json"));
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            decompile(chooser.getSelectedFile().toPath());
        }
    }

    @Override
    public void decompile(Path exportFile) {
        Path target = project != null ? projectView.selectedFolder().orElse(project.root()) : exportFile.getParent();
        try {
            BuildService.DecompileOutcome outcome = builds.decompile(exportFile, target);
            buildOutput.clear();
            window.showBuildOutput();
            outcome.files().forEach(file -> buildOutput.println("Decompiled to " + file));
            outcome.warnings().forEach(warning -> buildOutput.println("Note: " + warning));
            projectView.refresh();
            outcome.files().forEach(this::openFile);
        } catch (IOException e) {
            showError("The file could not be read", e);
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage());
        }
    }

    // ----- edit and view -----------------------------------------------------------------

    void findReplace(boolean replace) {
        if (currentTextArea() != null) {
            findReplace.showFor(replace);
        }
    }

    void goToLine() {
        RSyntaxTextArea textArea = currentTextArea();
        if (textArea == null) {
            return;
        }
        String input = JOptionPane.showInputDialog(window, "Line number:", textArea.getCaretLineNumber() + 1);
        try {
            int line = Integer.parseInt(input.strip()) - 1;
            textArea.setCaretPosition(textArea.getLineStartOffset(Math.max(0, Math.min(line, textArea.getLineCount() - 1))));
            textArea.requestFocusInWindow();
        } catch (NumberFormatException | NullPointerException | javax.swing.text.BadLocationException e) {
            // No valid line number was entered; nothing to do.
        }
    }

    void toggleTheme() {
        boolean dark = !settings.darkTheme();
        settings.setDarkTheme(dark);
        Themes.apply(dark);
        Themes.updateOpenWindows();
        tabs.applyTheme(theme());
    }

    void changeFontSize(int delta) {
        int size = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, settings.fontSize() + delta));
        settings.setFontSize(size);
        tabs.tabs().forEach(tab -> tab.setFontSize(size));
    }

    RSyntaxTextArea currentTextArea() {
        return tabs.current().map(EditorTab::textArea).orElse(null);
    }

    void openReference() {
        try (InputStream in = IdeController.class.getResourceAsStream(REFERENCE_RESOURCE)) {
            if (in == null) {
                showMessage("The language reference is not in this build.");
                return;
            }
            Path file = Files.createTempFile("MacroScript_Reference", ".html");
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            Desktop.getDesktop().browse(file.toUri());
        } catch (IOException | UnsupportedOperationException e) {
            showError("The language reference could not be opened", e);
        }
    }

    void about() {
        JOptionPane.showMessageDialog(window, "MacroScript IDE\n\nMacroScript " + mapping.macroScriptVersion()
                        + " for MacroDroid " + mapping.macroDroidVersion() + "\n"
                        + mapping.commands(CommandCategory.TRIGGER).size() + " triggers, "
                        + mapping.commands(CommandCategory.CONSTRAINT).size() + " constraints, "
                        + mapping.commands(CommandCategory.ACTION).size() + " actions",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----- EditorServices ----------------------------------------------------------------

    @Override
    public CompileResult compile(Path file, String text) {
        Path root = project != null ? project.root() : (file != null ? file.getParent() : null);
        return builds.compile(file, text, root, tabs::openText);
    }

    @Override
    public CompletionProvider completions() {
        return completions;
    }

    @Override
    public EditorTheme theme() {
        return Themes.editorTheme(settings.darkTheme());
    }

    @Override
    public int fontSize() {
        return settings.fontSize();
    }

    @Override
    public void diagnosticsChanged(EditorTab tab, List<Diagnostic> diagnostics) {
        if (tabs.current().orElse(null) == tab) {
            showProblems(diagnostics);
        }
    }

    @Override
    public void dirtyChanged(EditorTab tab) {
        tabs.updateTitle(tab);
    }

    @Override
    public void caretMoved(EditorTab tab) {
        if (tabs.current().orElse(null) == tab) {
            updateCaret(tab.textArea());
        }
    }

    // ----- helpers -----------------------------------------------------------------------

    private void currentTabChanged() {
        Optional<EditorTab> current = tabs.current();
        window.showEditors(current.isPresent());
        showProblems(current.map(EditorTab::diagnostics).orElse(List.of()));
        current.ifPresentOrElse(tab -> updateCaret(tab.textArea()), statusBar::clearPosition);
    }

    private void updateCaret(RSyntaxTextArea textArea) {
        statusBar.setPosition(textArea.getCaretLineNumber() + 1, textArea.getCaretOffsetFromLineStart() + 1);
    }

    private void showProblems(List<Diagnostic> diagnostics) {
        problems.show(diagnostics);
        window.setProblemsTitle(diagnostics.isEmpty() ? "Problems" : "Problems (" + ProblemsPanel.summary(diagnostics) + ")");
    }

    private void navigateTo(Diagnostic diagnostic) {
        if (diagnostic.file() != null && tabs.current().map(EditorTab::file).filter(diagnostic.file()::equals).isEmpty()) {
            openFile(diagnostic.file());
        }
        tabs.current().ifPresent(tab -> tab.goTo(diagnostic.range().start()));
    }

    private Optional<Path> chooseFolder(String title) {
        JFileChooser chooser = fileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION
                ? Optional.of(chooser.getSelectedFile().toPath())
                : Optional.empty();
    }

    private JFileChooser fileChooser() {
        JFileChooser chooser = new JFileChooser();
        if (project != null) {
            chooser.setCurrentDirectory(project.root().toFile());
        }
        return chooser;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(window, message, "MacroScript IDE", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(window, message + ":\n" + e.getMessage(), "MacroScript IDE", JOptionPane.ERROR_MESSAGE);
    }
}
