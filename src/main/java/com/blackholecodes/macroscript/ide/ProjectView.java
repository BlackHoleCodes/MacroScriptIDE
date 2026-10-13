package com.blackholecodes.macroscript.ide;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/** The project tool window: a tree of the files in the project folder. */
final class ProjectView extends JPanel {

    /** What the project view asks the IDE to do. */
    interface Listener {
        void openFile(Path file);

        void newFile(Path folder, boolean actionBlock);

        void newFolder(Path folder);

        void rename(Path path);

        void delete(Path path);

        void decompile(Path exportFile);

        void reveal(Path path);
    }

    private final JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("No project")));
    private final Listener listener;
    private Project project;

    ProjectView(Listener listener) {
        super(new BorderLayout());
        this.listener = listener;
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new FileRenderer());
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
                    selectedPath().filter(Files::isRegularFile).ifPresent(listener::openFile);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showMenu(e);
            }
        });
        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        tree.getActionMap().put("open", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPath().filter(Files::isRegularFile).ifPresent(listener::openFile);
            }
        });
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    void setProject(Project newProject) {
        project = newProject;
        refresh();
        tree.expandRow(0);
    }

    /** Reads the folder again. Folders that were open stay open. */
    void refresh() {
        if (project == null) {
            tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No project")));
            return;
        }
        Set<Path> expanded = expandedPaths();
        Optional<Path> selected = selectedPath();
        DefaultMutableTreeNode root = node(project.root());
        tree.setModel(new DefaultTreeModel(root));
        restoreExpanded(root, expanded);
        selected.ifPresent(path -> select(root, path));
    }

    private void select(DefaultMutableTreeNode root, Path path) {
        Enumeration<javax.swing.tree.TreeNode> nodes = root.breadthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            if (path.equals(node.getUserObject())) {
                tree.setSelectionPath(new TreePath(node.getPath()));
                return;
            }
        }
    }

    /** The selected file or folder. */
    Optional<Path> selectedPath() {
        TreePath selection = tree.getSelectionPath();
        if (selection == null || !(((DefaultMutableTreeNode) selection.getLastPathComponent()).getUserObject() instanceof Path path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    /** The selected folder, the folder of the selected file, or the project folder. */
    Optional<Path> selectedFolder() {
        Optional<Path> selected = selectedPath();
        if (selected.isPresent()) {
            return Optional.of(Files.isDirectory(selected.get()) ? selected.get() : selected.get().getParent());
        }
        return project == null ? Optional.empty() : Optional.of(project.root());
    }

    private DefaultMutableTreeNode node(Path path) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(path);
        if (Files.isDirectory(path)) {
            for (Path child : children(path)) {
                node.add(node(child));
            }
        }
        return node;
    }

    /** The visible children of a folder: folders first, then files, by name. Hidden files are left out. */
    private static List<Path> children(Path folder) {
        try (Stream<Path> entries = Files.list(folder)) {
            return entries.filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator.comparing((Path p) -> !Files.isDirectory(p))
                            .thenComparing(p -> p.getFileName().toString().toLowerCase(java.util.Locale.ROOT)))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Set<Path> expandedPaths() {
        Set<Path> expanded = new HashSet<>();
        Enumeration<TreePath> paths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        if (paths != null) {
            while (paths.hasMoreElements()) {
                Object node = ((DefaultMutableTreeNode) paths.nextElement().getLastPathComponent()).getUserObject();
                if (node instanceof Path path) {
                    expanded.add(path);
                }
            }
        }
        return expanded;
    }

    private void restoreExpanded(DefaultMutableTreeNode root, Set<Path> expanded) {
        List<DefaultMutableTreeNode> toExpand = new ArrayList<>();
        Enumeration<javax.swing.tree.TreeNode> nodes = root.breadthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            if (expanded.contains(node.getUserObject())) {
                toExpand.add(node);
            }
        }
        for (DefaultMutableTreeNode node : toExpand) {
            tree.expandPath(new TreePath(node.getPath()));
        }
    }

    private void showMenu(MouseEvent e) {
        if (!e.isPopupTrigger() || project == null) {
            return;
        }
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            tree.setSelectionPath(path);
        }
        Path folder = selectedFolder().orElse(project.root());
        Optional<Path> selected = selectedPath();

        JPopupMenu menu = new JPopupMenu();
        menu.add(item("New Macro...", () -> listener.newFile(folder, false)));
        menu.add(item("New Action Block...", () -> listener.newFile(folder, true)));
        menu.add(item("New Folder...", () -> listener.newFolder(folder)));
        if (selected.isPresent() && !selected.get().equals(project.root())) {
            menu.addSeparator();
            menu.add(item("Rename...", () -> listener.rename(selected.get())));
            menu.add(item("Delete", () -> listener.delete(selected.get())));
        }
        if (selected.isPresent() && isExport(selected.get())) {
            menu.addSeparator();
            menu.add(item("Decompile to MacroScript", () -> listener.decompile(selected.get())));
        }
        menu.addSeparator();
        menu.add(item("Show in File Manager", () -> listener.reveal(selected.orElse(project.root()))));
        menu.add(item("Refresh", this::refresh));
        menu.show(tree, e.getX(), e.getY());
    }

    private static boolean isExport(Path path) {
        String name = path.getFileName().toString();
        return Files.isRegularFile(path) && (name.endsWith(".json") || name.endsWith(".macro"));
    }

    private static JMenuItem item(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    /** Shows file names, and folder icons also for empty folders. */
    private static final class FileRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (((DefaultMutableTreeNode) value).getUserObject() instanceof Path path) {
                Path fileName = path.getFileName();
                setText(fileName == null ? path.toString() : fileName.toString());
                if (Files.isDirectory(path)) {
                    setIcon(expanded ? getOpenIcon() : getClosedIcon());
                }
            }
            return this;
        }
    }
}
