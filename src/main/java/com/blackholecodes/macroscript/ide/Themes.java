package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.ide.editor.EditorTheme;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.UIManager;

/** Switches between the light (IntelliJ) and dark (Darcula) look. */
final class Themes {

    private Themes() {
    }

    /** Sets the look and feel. Call {@link #updateOpenWindows()} afterwards when windows are already shown. */
    static void apply(boolean dark) {
        // FlatLaf hides tree icons by default; folders and files are easier to tell apart with them.
        UIManager.put("Tree.showDefaultIcons", true);
        if (dark) {
            FlatDarculaLaf.setup();
        } else {
            FlatIntelliJLaf.setup();
        }
    }

    static void updateOpenWindows() {
        FlatLaf.updateUI();
    }

    static EditorTheme editorTheme(boolean dark) {
        return dark ? EditorTheme.DARK : EditorTheme.LIGHT;
    }
}
