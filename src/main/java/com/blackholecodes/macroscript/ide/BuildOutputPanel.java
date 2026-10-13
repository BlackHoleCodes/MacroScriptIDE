package com.blackholecodes.macroscript.ide;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** The Build tool window: what the last builds did. */
final class BuildOutputPanel extends JPanel {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JTextArea text = new JTextArea();

    BuildOutputPanel() {
        super(new BorderLayout());
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(text), BorderLayout.CENTER);
    }

    void clear() {
        text.setText("");
    }

    void println(String line) {
        text.append(LocalTime.now().format(TIME) + "  " + line + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
}
