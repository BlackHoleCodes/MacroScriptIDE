package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/** Turns ANTLR syntax errors into diagnostics with messages a user can act on. */
final class SyntaxErrorListener extends BaseErrorListener {

    private final Diagnostics diagnostics;
    private final SourceOrigin origin;

    SyntaxErrorListener(Diagnostics diagnostics, SourceOrigin origin) {
        this.diagnostics = diagnostics;
        this.origin = origin;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String message, RecognitionException e) {
        if (offendingSymbol instanceof Token token) {
            diagnostics.error(origin.range(token), describe(token, message));
        } else {
            int offset = origin.offset();
            diagnostics.error(new SourceRange(offset, offset + 1, origin.line() + line - 1, charPositionInLine), message);
        }
    }

    private static String describe(Token token, String antlrMessage) {
        return switch (token.getType()) {
            case MacroScriptLexer.UNTERMINATED_STRING -> "This string has no closing quote.";
            case MacroScriptLexer.UNTERMINATED_COMMENT -> "This comment has no closing */.";
            case MacroScriptLexer.ERROR_CHARACTER -> "Unexpected character '" + token.getText() + "'.";
            case Token.EOF -> "Unexpected end of file. " + capitalize(antlrMessage.replace("<EOF>", "end of file"));
            default -> capitalize(antlrMessage) + ".";
        };
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
