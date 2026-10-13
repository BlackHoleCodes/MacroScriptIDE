package com.blackholecodes.macroscript.ide;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/** The line at the bottom of the window: a message on the left, the caret position on the right. */
final class StatusBar extends JPanel {

    private final JLabel message = new JLabel(" ");
    private final JLabel position = new JLabel(" ");

    StatusBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(message, BorderLayout.CENTER);
        add(position, BorderLayout.LINE_END);
    }

    void setMessage(String text) {
        message.setText(text);
    }

    void setPosition(int line, int column) {
        position.setText(line + ":" + column);
    }

    void clearPosition() {
        position.setText(" ");
    }
}
