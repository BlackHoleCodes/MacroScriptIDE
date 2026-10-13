package com.blackholecodes.macroscript.ide;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * The main window. It only arranges the parts: the project tree on the left, the editor
 * tabs in the middle, the Problems and Build tool windows at the bottom.
 * {@link IdeController} decides what the parts do.
 */
final class MainWindow extends JFrame {

    private static final String EDITORS_CARD = "editors";
    private static final String WELCOME_CARD = "welcome";
    private static final int PROBLEMS_TAB = 0;
    private static final int BUILD_TAB = 1;

    private final JTabbedPane toolWindows = new JTabbedPane(JTabbedPane.TOP);
    private final CardLayout centerCards = new CardLayout();
    private final JPanel center = new JPanel(centerCards);

    MainWindow(ProjectView projectView, EditorTabs tabs, ProblemsPanel problems, BuildOutputPanel buildOutput,
               StatusBar statusBar) {
        super("MacroScript IDE");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));

        center.add(tabs, EDITORS_CARD);
        center.add(welcomePanel(), WELCOME_CARD);

        toolWindows.addTab("Problems", problems);
        toolWindows.addTab("Build", buildOutput);

        JSplitPane editorAndTools = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, toolWindows);
        editorAndTools.setResizeWeight(0.78);
        editorAndTools.setBorder(null);

        projectView.setPreferredSize(new Dimension(250, 400));
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectView, editorAndTools);
        main.setResizeWeight(0.0);
        main.setBorder(null);

        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.PAGE_END);
        setSize(1280, 820);
        setLocationRelativeTo(null);
    }

    /** Shows the editors, or the welcome text when no file is open. */
    void showEditors(boolean anyOpen) {
        centerCards.show(center, anyOpen ? EDITORS_CARD : WELCOME_CARD);
    }

    void showProblems() {
        toolWindows.setSelectedIndex(PROBLEMS_TAB);
    }

    void showBuildOutput() {
        toolWindows.setSelectedIndex(BUILD_TAB);
    }

    void setProblemsTitle(String title) {
        toolWindows.setTitleAt(PROBLEMS_TAB, title);
    }

    private static JPanel welcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><div style='text-align:center'>"
                + "<h2>MacroScript IDE</h2>"
                + "Open a project folder with <b>File &gt; Open Project</b>,<br>"
                + "or make one with <b>File &gt; New Project</b>.<br><br>"
                + "Then double-click a <b>.ms</b> file in the project tree.</div></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
