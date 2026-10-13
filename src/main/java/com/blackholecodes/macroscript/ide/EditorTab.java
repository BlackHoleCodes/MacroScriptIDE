package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.ide.editor.CompletionSetup;
import com.blackholecodes.macroscript.ide.editor.DiagnosticsParser;
import com.blackholecodes.macroscript.ide.editor.EditorTheme;
import com.blackholecodes.macroscript.ide.editor.MacroScriptLanguage;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** One open file: the text area with line numbers, folding and live error underlines. */
final class EditorTab extends JPanel {

    /** How long to wait after typing stops before the text is compiled again. */
    private static final int DIAGNOSTICS_DELAY_MS = 600;

    private final RSyntaxTextArea textArea = new RSyntaxTextArea(30, 100);
    private final DiagnosticsParser diagnosticsParser;
    private final EditorServices services;
    private Path file;
    private boolean dirty;
    private List<Diagnostic> diagnostics = List.of();

    EditorTab(Path file, String text, EditorServices services) {
        super(new BorderLayout());
        this.file = file;
        this.services = services;

        textArea.setSyntaxEditingStyle(MacroScriptLanguage.SYNTAX_STYLE);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setCloseCurlyBraces(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setPaintMatchedBracketPair(true);
        textArea.setMarkOccurrences(true);
        textArea.setTabSize(4);
        textArea.setTabsEmulated(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, services.fontSize()));
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.discardAllEdits();
        applyTheme(services.theme());

        diagnosticsParser = new DiagnosticsParser(file, source -> services.compile(this.file, source), this::onDiagnostics);
        textArea.addParser(diagnosticsParser);
        textArea.setParserDelay(DIAGNOSTICS_DELAY_MS);
        CompletionSetup.install(textArea, services.completions());

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        add(new ErrorStrip(textArea), BorderLayout.LINE_END);

        textArea.addCaretListener(e -> services.caretMoved(this));
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markDirty();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                markDirty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Style changes do not change the text.
            }
        });
    }

    RSyntaxTextArea textArea() {
        return textArea;
    }

    /** The file, or null for a new file that has not been saved yet. */
    Path file() {
        return file;
    }

    String text() {
        return textArea.getText();
    }

    boolean isDirty() {
        return dirty;
    }

    List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** The tab title: the file name, with a star when there are unsaved changes. */
    String title() {
        String name = file == null ? "Untitled" : file.getFileName().toString();
        return dirty ? "*" + name : name;
    }

    void save() throws IOException {
        Files.writeString(file, textArea.getText());
        setDirty(false);
    }

    void saveAs(Path newFile) throws IOException {
        file = newFile;
        diagnosticsParser.setFile(newFile);
        save();
    }

    /** Called when the file was renamed or moved in the project view. */
    void fileMoved(Path newFile) {
        file = newFile;
        diagnosticsParser.setFile(newFile);
        services.dirtyChanged(this);
    }

    /** Moves the caret to an offset and shows that line. */
    void goTo(int offset) {
        textArea.setCaretPosition(Math.max(0, Math.min(offset, textArea.getDocument().getLength())));
        textArea.requestFocusInWindow();
    }

    void applyTheme(EditorTheme theme) {
        theme.applyTo(textArea);
    }

    void setFontSize(int size) {
        textArea.setFont(textArea.getFont().deriveFont((float) size));
        applyTheme(services.theme());
    }

    private void onDiagnostics(List<Diagnostic> newDiagnostics) {
        diagnostics = newDiagnostics;
        services.diagnosticsChanged(this, newDiagnostics);
    }

    private void markDirty() {
        setDirty(true);
    }

    private void setDirty(boolean value) {
        if (dirty != value) {
            dirty = value;
            services.dirtyChanged(this);
        }
    }
}
