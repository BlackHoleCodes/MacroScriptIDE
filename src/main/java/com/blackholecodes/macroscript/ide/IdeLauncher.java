package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.ide.editor.MacroScriptLanguage;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Starts the IDE window. */
public final class IdeLauncher {

    private IdeLauncher() {
    }

    /**
     * @param args an optional project folder to open
     */
    public static void launch(String[] args) {
        Optional<Path> project = args.length > 0 && Files.isDirectory(Path.of(args[0]))
                ? Optional.of(Path.of(args[0]))
                : Optional.empty();
        SwingUtilities.invokeLater(() -> {
            IdeSettings settings = new IdeSettings();
            Themes.apply(settings.darkTheme());
            MacroScriptLanguage.register();
            Thread.setDefaultUncaughtExceptionHandler((thread, error) -> SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "Something went wrong: " + error,
                            "MacroScript IDE", JOptionPane.ERROR_MESSAGE)));
            new IdeController(settings).start(project.or(settings::lastProject));
        });
    }
}
