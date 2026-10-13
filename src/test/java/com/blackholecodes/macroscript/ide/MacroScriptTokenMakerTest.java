package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.ide.editor.MacroScriptTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.junit.jupiter.api.Test;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroScriptTokenMakerTest {

    private final MacroScriptTokenMaker tokenMaker = new MacroScriptTokenMaker();

    /** The tokens of a line as "type:text" pairs, without whitespace. */
    private List<String> tokens(String line, int initialType) {
        Segment segment = new Segment(line.toCharArray(), 0, line.length());
        List<String> result = new ArrayList<>();
        for (Token t = tokenMaker.getTokenList(segment, initialType, 0); t != null && t.isPaintable(); t = t.getNextToken()) {
            if (t.getType() != Token.WHITESPACE) {
                result.add(t.getType() + ":" + t.getLexeme());
            }
        }
        return result;
    }

    @Test
    void coloursKeywordsCallsConstantsAndStrings() {
        List<String> tokens = tokens("if (x) setWifi(TURN_ON, \"a ${b} c\");", Token.NULL);
        assertTrue(tokens.contains(Token.RESERVED_WORD + ":if"));
        assertTrue(tokens.contains(Token.IDENTIFIER + ":x"));
        assertTrue(tokens.contains(Token.FUNCTION + ":setWifi"));
        assertTrue(tokens.contains(Token.PREPROCESSOR + ":TURN_ON"));
        assertTrue(tokens.contains(Token.LITERAL_STRING_DOUBLE_QUOTE + ":\"a "));
        assertTrue(tokens.contains(Token.VARIABLE + ":${b}"));
        assertTrue(tokens.contains(Token.LITERAL_STRING_DOUBLE_QUOTE + ": c\""));
    }

    @Test
    void continuesABlockCommentOnTheNextLine() {
        Segment first = new Segment("x = 1; /* start".toCharArray(), 0, 15);
        Token last = lastToken(tokenMaker.getTokenList(first, Token.NULL, 0));
        assertEquals(Token.COMMENT_MULTILINE, last.getType());

        List<String> second = tokens("end */ y", Token.COMMENT_MULTILINE);
        assertEquals(List.of(Token.COMMENT_MULTILINE + ":end */", Token.IDENTIFIER + ":y"), second);
    }

    @Test
    void coloursAnnotationsNumbersAndComments() {
        List<String> tokens = tokens("@name(\"x\") 30s #FF0000 // done", Token.NULL);
        assertTrue(tokens.contains(Token.ANNOTATION + ":@name"));
        assertTrue(tokens.contains(Token.LITERAL_NUMBER_DECIMAL_INT + ":30s"));
        assertTrue(tokens.contains(Token.LITERAL_NUMBER_HEXADECIMAL + ":#FF0000"));
        assertTrue(tokens.contains(Token.COMMENT_EOL + ":// done"));
    }

    private static Token lastToken(Token token) {
        Token last = token;
        while (last.getNextToken() != null && last.getNextToken().isPaintable()) {
            last = last.getNextToken();
        }
        return last;
    }
}
