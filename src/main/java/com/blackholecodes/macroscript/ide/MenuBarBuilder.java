package com.blackholecodes.macroscript.ide;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Builds the menu bar. Each item calls one method of {@link IdeController}. The shortcuts
 * follow IntelliJ IDEA where it has one; Ctrl is Cmd on macOS.
 */
final class MenuBarBuilder {

    /** Ctrl on Windows and Linux, Cmd on macOS. */
    private static final int MENU_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    private static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;

    private final IdeController ide;

    MenuBarBuilder(IdeController ide) {
        this.ide = ide;
    }

    JMenuBar build() {
        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu());
        bar.add(editMenu());
        bar.add(viewMenu());
        bar.add(buildMenu());
        bar.add(helpMenu());
        return bar;
    }

    private JMenu fileMenu() {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(item("New Project...", null, ide::newProject));
        menu.add(item("Open Project...", key(KeyEvent.VK_O, MENU_KEY | SHIFT), ide::openProjectDialog));
        menu.add(recentProjectsMenu());
        menu.addSeparator();
        menu.add(item("New Macro...", key(KeyEvent.VK_N, MENU_KEY), () -> ide.newFileDialog(false)));
        menu.add(item("New Action Block...", key(KeyEvent.VK_N, MENU_KEY | SHIFT), () -> ide.newFileDialog(true)));
        menu.add(item("Open File...", key(KeyEvent.VK_O, MENU_KEY), ide::openFileDialog));
        menu.addSeparator();
        menu.add(item("Save", key(KeyEvent.VK_S, MENU_KEY), ide::save));
        menu.add(item("Save As...", null, ide::saveAs));
        menu.add(item("Save All", key(KeyEvent.VK_S, MENU_KEY | SHIFT), ide::saveAll));
        menu.add(item("Close Tab", key(KeyEvent.VK_W, MENU_KEY), ide::closeTab));
        menu.addSeparator();
        menu.add(item("Decompile MacroDroid Export...", null, ide::decompileDialog));
        menu.addSeparator();
        menu.add(item("Exit", null, ide::exit));
        return menu;
    }

    private JMenu recentProjectsMenu() {
        JMenu menu = new JMenu("Recent Projects");
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                menu.removeAll();
                for (Path project : ide.recentProjects()) {
                    menu.add(item(project.toString(), null, () -> ide.openProject(project)));
                }
                if (menu.getItemCount() == 0) {
                    JMenuItem none = new JMenuItem("No recent projects");
                    none.setEnabled(false);
                    menu.add(none);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // Nothing to do.
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // Nothing to do.
            }
        });
        return menu;
    }

    private JMenu editMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(editorItem("Undo", key(KeyEvent.VK_Z, MENU_KEY), RTextArea::undoLastAction));
        menu.add(editorItem("Redo", key(KeyEvent.VK_Z, MENU_KEY | SHIFT), RTextArea::redoLastAction));
        menu.addSeparator();
        menu.add(editorItem("Cut", key(KeyEvent.VK_X, MENU_KEY), RTextArea::cut));
        menu.add(editorItem("Copy", key(KeyEvent.VK_C, MENU_KEY), RTextArea::copy));
        menu.add(editorItem("Paste", key(KeyEvent.VK_V, MENU_KEY), RTextArea::paste));
        menu.add(editorItem("Select All", key(KeyEvent.VK_A, MENU_KEY), RTextArea::selectAll));
        menu.addSeparator();
        menu.add(item("Find...", key(KeyEvent.VK_F, MENU_KEY), () -> ide.findReplace(false)));
        menu.add(item("Replace...", key(KeyEvent.VK_R, MENU_KEY), () -> ide.findReplace(true)));
        menu.add(item("Go to Line...", key(KeyEvent.VK_G, MENU_KEY), ide::goToLine));
        menu.addSeparator();
        menu.add(editorItem("Toggle Line Comment", key(KeyEvent.VK_SLASH, MENU_KEY),
                textArea -> textArea.getActionMap().get(RSyntaxTextAreaEditorKit.rstaToggleCommentAction)
                        .actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""))));
        return menu;
    }

    private JMenu viewMenu() {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.add(item("Toggle Dark Theme", null, ide::toggleTheme));
        menu.add(item("Larger Font", key(KeyEvent.VK_EQUALS, MENU_KEY), () -> ide.changeFontSize(1)));
        menu.add(item("Smaller Font", key(KeyEvent.VK_MINUS, MENU_KEY), () -> ide.changeFontSize(-1)));
        return menu;
    }

    private JMenu buildMenu() {
        JMenu menu = new JMenu("Build");
        menu.setMnemonic(KeyEvent.VK_B);
        menu.add(item("Build Project", key(KeyEvent.VK_F9, MENU_KEY), ide::buildProject));
        menu.add(item("Build Current File", key(KeyEvent.VK_F9, MENU_KEY | SHIFT), ide::buildCurrentFile));
        menu.addSeparator();
        menu.add(item("Open Output Folder", null, ide::openOutputFolder));
        return menu;
    }

    private JMenu helpMenu() {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(item("MacroScript Reference", key(KeyEvent.VK_F1, 0), ide::openReference));
        menu.add(item("About", null, ide::about));
        return menu;
    }

    private static JMenuItem item(String text, KeyStroke shortcut, Runnable handler) {
        Action action = new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.run();
            }
        };
        JMenuItem item = new JMenuItem(action);
        if (shortcut != null) {
            item.setAccelerator(shortcut);
        }
        return item;
    }

    /** A menu item that works on the current editor and does nothing when no file is open. */
    private JMenuItem editorItem(String text, KeyStroke shortcut, Consumer<RSyntaxTextArea> handler) {
        return item(text, shortcut, () -> {
            RSyntaxTextArea textArea = ide.currentTextArea();
            if (textArea != null) {
                handler.accept(textArea);
            }
        });
    }

    private static KeyStroke key(int keyCode, int modifiers) {
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }
}
