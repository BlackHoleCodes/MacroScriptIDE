package com.blackholecodes.macroscript.ide;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

/** Find and replace in the current editor. */
final class FindReplaceDialog extends JDialog {

    private final Supplier<RSyntaxTextArea> editor;
    private final JTextField findField = new JTextField(28);
    private final JTextField replaceField = new JTextField(28);
    private final JCheckBox matchCase = new JCheckBox("Match case");
    private final JCheckBox wholeWord = new JCheckBox("Whole words");
    private final JCheckBox regex = new JCheckBox("Regex");
    private final JLabel status = new JLabel(" ");

    FindReplaceDialog(JFrame owner, Supplier<RSyntaxTextArea> editor) {
        super(owner, "Find and Replace", false);
        this.editor = editor;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Find:"), c);
        c.gridx = 1;
        panel.add(findField, c);
        c.gridx = 2;
        panel.add(button("Next", () -> find(true)), c);
        c.gridx = 3;
        panel.add(button("Previous", () -> find(false)), c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Replace:"), c);
        c.gridx = 1;
        panel.add(replaceField, c);
        c.gridx = 2;
        panel.add(button("Replace", this::replace), c);
        c.gridx = 3;
        panel.add(button("Replace All", this::replaceAll), c);

        JPanel options = new JPanel();
        options.add(matchCase);
        options.add(wholeWord);
        options.add(regex);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 3;
        panel.add(options, c);
        c.gridy = 3;
        panel.add(status, c);

        findField.addActionListener(e -> find(true));
        replaceField.addActionListener(e -> replace());
        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Shows the dialog with the selected text as the search text. */
    void showFor(boolean focusReplace) {
        RSyntaxTextArea textArea = editor.get();
        if (textArea != null && textArea.getSelectedText() != null && !textArea.getSelectedText().contains("\n")) {
            findField.setText(textArea.getSelectedText());
        }
        status.setText(" ");
        setVisible(true);
        (focusReplace ? replaceField : findField).requestFocusInWindow();
        findField.selectAll();
    }

    private void find(boolean forward) {
        run(context -> {
            context.setSearchForward(forward);
            return SearchEngine.find(editor.get(), context);
        });
    }

    private void replace() {
        run(context -> {
            context.setSearchForward(true);
            return SearchEngine.replace(editor.get(), context);
        });
    }

    private void replaceAll() {
        run(context -> SearchEngine.replaceAll(editor.get(), context));
    }

    private void run(java.util.function.Function<SearchContext, SearchResult> search) {
        if (editor.get() == null || findField.getText().isEmpty()) {
            return;
        }
        SearchContext context = new SearchContext(findField.getText());
        context.setReplaceWith(replaceField.getText());
        context.setMatchCase(matchCase.isSelected());
        context.setWholeWord(wholeWord.isSelected());
        context.setRegularExpression(regex.isSelected());
        try {
            SearchResult result = search.apply(context);
            status.setText(result.wasFound() ? result.getCount() + " found" : "Not found");
        } catch (RuntimeException e) {
            // An invalid regular expression.
            status.setText(e.getMessage());
        }
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }
}
