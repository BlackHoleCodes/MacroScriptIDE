package com.blackholecodes.macroscript.ide.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * The editor colours, close to IntelliJ IDEA's light and Darcula schemes. The base theme
 * comes with RSyntaxTextArea; the token colours are set here.
 */
public enum EditorTheme {

    LIGHT("/org/fife/ui/rsyntaxtextarea/themes/idea.xml",
            new Palette(0x0033B3, 0x067D17, 0x0037A6, 0x1750EB, 0x9E880D, 0x871094, 0x00627A, 0x8C8C8C, 0x8C8C8C, 0x080808)),
    DARK("/org/fife/ui/rsyntaxtextarea/themes/dark.xml",
            new Palette(0xCC7832, 0x6A8759, 0xCC7832, 0x6897BB, 0xBBB529, 0x9876AA, 0xFFC66D, 0x808080, 0x629755, 0xA9B7C6));

    /** The colours of the token groups. */
    private record Palette(int keyword, int string, int interpolation, int number, int annotation, int constant,
                           int function, int comment, int docComment, int text) {
    }

    private final String baseTheme;
    private final Palette palette;

    EditorTheme(String baseTheme, Palette palette) {
        this.baseTheme = baseTheme;
        this.palette = palette;
    }

    public void applyTo(RSyntaxTextArea textArea) {
        Font font = textArea.getFont();
        try (InputStream in = EditorTheme.class.getResourceAsStream(baseTheme)) {
            Theme.load(in).apply(textArea);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load the editor theme " + baseTheme, e);
        }
        textArea.setFont(font);

        SyntaxScheme scheme = textArea.getSyntaxScheme();
        Font plain = font.deriveFont(Font.PLAIN);
        Font bold = font.deriveFont(Font.BOLD);
        Font italic = font.deriveFont(Font.ITALIC);
        set(scheme, Token.RESERVED_WORD, palette.keyword, bold);
        set(scheme, Token.LITERAL_BOOLEAN, palette.keyword, bold);
        set(scheme, Token.LITERAL_STRING_DOUBLE_QUOTE, palette.string, plain);
        set(scheme, MacroScriptTokenMaker.INTERPOLATION, palette.interpolation, bold);
        set(scheme, Token.LITERAL_NUMBER_DECIMAL_INT, palette.number, plain);
        set(scheme, Token.LITERAL_NUMBER_FLOAT, palette.number, plain);
        set(scheme, Token.LITERAL_NUMBER_HEXADECIMAL, palette.number, plain);
        set(scheme, Token.ANNOTATION, palette.annotation, plain);
        set(scheme, MacroScriptTokenMaker.CONSTANT, palette.constant, italic);
        set(scheme, Token.FUNCTION, palette.function, plain);
        set(scheme, Token.COMMENT_EOL, palette.comment, italic);
        set(scheme, Token.COMMENT_MULTILINE, palette.comment, italic);
        set(scheme, Token.COMMENT_DOCUMENTATION, palette.docComment, italic);
        set(scheme, Token.IDENTIFIER, palette.text, plain);
        set(scheme, Token.OPERATOR, palette.text, plain);
        set(scheme, Token.SEPARATOR, palette.text, plain);
        textArea.revalidate();
        textArea.repaint();
    }

    private static void set(SyntaxScheme scheme, int tokenType, int rgb, Font font) {
        Style style = scheme.getStyle(tokenType);
        style.foreground = new Color(rgb);
        style.font = font;
        style.background = null;
    }
}
