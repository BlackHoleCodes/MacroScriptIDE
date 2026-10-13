package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.ide.editor.EditorTheme;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JTabbedPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/** The editor tabs. Each open file has one tab; a tab can be closed with its close button. */
final class EditorTabs extends JTabbedPane {

    private final EditorServices services;
    private final Predicate<EditorTab> confirmClose;

    /**
     * @param confirmClose asks what to do with unsaved changes; false keeps the tab open
     */
    EditorTabs(EditorServices services, Predicate<EditorTab> confirmClose) {
        this.services = services;
        this.confirmClose = confirmClose;
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (IntConsumer) index -> close(tabAt(index)));
    }

    /** Opens a file, or selects its tab when it is already open. */
    EditorTab open(Path file) throws IOException {
        Path normalized = file.toAbsolutePath().normalize();
        Optional<EditorTab> existing = find(normalized);
        if (existing.isPresent()) {
            setSelectedComponent(existing.get());
            return existing.get();
        }
        return add(new EditorTab(normalized, Files.readString(normalized), services));
    }

    /** Opens an empty tab for a file that is not saved yet. */
    EditorTab openUntitled(String text) {
        return add(new EditorTab(null, text, services));
    }

    private EditorTab add(EditorTab tab) {
        addTab(tab.title(), tab);
        int index = indexOfComponent(tab);
        setToolTipTextAt(index, tab.file() == null ? "Not saved yet" : tab.file().toString());
        setSelectedIndex(index);
        tab.textArea().requestFocusInWindow();
        return tab;
    }

    Optional<EditorTab> current() {
        return getSelectedComponent() instanceof EditorTab tab ? Optional.of(tab) : Optional.empty();
    }

    Optional<EditorTab> find(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        return tabs().stream().filter(tab -> normalized.equals(tab.file())).findFirst();
    }

    /** The editor text of an open file, so the compiler can use changes that are not saved. */
    Optional<String> openText(Path file) {
        return find(file).map(EditorTab::text);
    }

    List<EditorTab> tabs() {
        List<EditorTab> tabs = new ArrayList<>();
        for (int i = 0; i < getTabCount(); i++) {
            tabs.add(tabAt(i));
        }
        return tabs;
    }

    /** The files of the open tabs, in tab order. */
    List<Path> openFiles() {
        return tabs().stream().map(EditorTab::file).filter(java.util.Objects::nonNull).toList();
    }

    /**
     * Closes a tab after asking about unsaved changes.
     *
     * @return false when the user chose to keep the tab
     */
    boolean close(EditorTab tab) {
        if (tab.isDirty() && !confirmClose.test(tab)) {
            return false;
        }
        remove(tab);
        return true;
    }

    /** Closes every tab. Returns false when the user kept one of them open. */
    boolean closeAll() {
        for (EditorTab tab : tabs()) {
            if (!close(tab)) {
                return false;
            }
        }
        return true;
    }

    /** Closes the tab of a file without asking, for example after the file was deleted. */
    void discard(Path file) {
        find(file).ifPresent(this::remove);
    }

    void updateTitle(EditorTab tab) {
        int index = indexOfComponent(tab);
        if (index >= 0) {
            setTitleAt(index, tab.title());
            setToolTipTextAt(index, tab.file() == null ? "Not saved yet" : tab.file().toString());
        }
    }

    void applyTheme(EditorTheme theme) {
        tabs().forEach(tab -> tab.applyTheme(theme));
    }

    private EditorTab tabAt(int index) {
        return (EditorTab) getComponentAt(index);
    }
}
