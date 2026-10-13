package com.blackholecodes.macroscript.ide.editor;

import com.blackholecodes.macroscript.parser.MacroScriptLexer;
import org.antlr.v4.runtime.CharStreams;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

/**
 * Colours MacroScript code in the editor. Each line is split into tokens by the lexer that
 * ANTLR generates from the grammar, so the colouring always matches the language.
 *
 * <p>RSyntaxTextArea asks for one line at a time. A block comment that is still open at the
 * end of a line is continued on the next line through {@code initialTokenType}.
 */
public final class MacroScriptTokenMaker extends AbstractTokenMaker {

    /** RSyntaxTextArea token types used for the parts of MacroScript that have no type of their own. */
    static final int CONSTANT = Token.PREPROCESSOR;
    static final int INTERPOLATION = Token.VARIABLE;

    @Override
    public TokenMap getWordsToHighlight() {
        // Keywords are found by the lexer, not by a word list.
        return new TokenMap();
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();
        char[] array = text.array;
        int lineStart = text.offset;
        int lineEnd = text.offset + text.count;
        int position = lineStart;

        // Continue a block comment from the line before.
        if (initialTokenType == Token.COMMENT_MULTILINE || initialTokenType == Token.COMMENT_DOCUMENTATION) {
            int close = indexOf(array, position, lineEnd, "*/");
            if (close < 0) {
                addToken(array, position, lineEnd - 1, initialTokenType, startOffset);
                return firstToken;
            }
            addToken(array, position, close + 1, initialTokenType, startOffset);
            position = close + 2;
        }

        boolean openComment = false;
        if (position < lineEnd) {
            openComment = addLexerTokens(array, position, lineEnd, startOffset + (position - lineStart));
        }
        if (!openComment) {
            addNullToken();
        }
        return firstToken;
    }

    /**
     * Lexes {@code array[from, to)} and adds its tokens.
     *
     * @return true when the line ends inside a block comment
     */
    private boolean addLexerTokens(char[] array, int from, int to, int documentOffset) {
        MacroScriptLexer lexer = new MacroScriptLexer(CharStreams.fromString(new String(array, from, to - from)));
        lexer.removeErrorListeners();
        List<org.antlr.v4.runtime.Token> tokens = new ArrayList<>(lexer.getAllTokens());
        boolean openComment = false;
        for (int i = 0; i < tokens.size(); i++) {
            org.antlr.v4.runtime.Token token = tokens.get(i);
            int start = from + token.getStartIndex();
            int end = from + token.getStopIndex();
            int offset = documentOffset + token.getStartIndex();
            int type = token.getType();
            if (type == MacroScriptLexer.STRING_LITERAL || type == MacroScriptLexer.UNTERMINATED_STRING) {
                addStringTokens(array, start, end, offset, type == MacroScriptLexer.STRING_LITERAL
                        ? Token.LITERAL_STRING_DOUBLE_QUOTE : Token.ERROR_STRING_DOUBLE);
                continue;
            }
            if (type == MacroScriptLexer.UNTERMINATED_COMMENT) {
                openComment = true;
            }
            addToken(array, start, end, tokenTypeOf(token, tokens, i), offset);
        }
        return openComment;
    }

    /** Maps one lexer token to an RSyntaxTextArea token type. */
    private static int tokenTypeOf(org.antlr.v4.runtime.Token token, List<org.antlr.v4.runtime.Token> tokens, int index) {
        int type = token.getType();
        if (Keywords.isKeywordToken(type)) {
            return switch (type) {
                case MacroScriptLexer.TRUE, MacroScriptLexer.FALSE, MacroScriptLexer.NULL -> Token.LITERAL_BOOLEAN;
                default -> Token.RESERVED_WORD;
            };
        }
        return switch (type) {
            case MacroScriptLexer.IDENT -> identifierType(token, tokens, index);
            case MacroScriptLexer.ANNOTATION -> Token.ANNOTATION;
            case MacroScriptLexer.INT_LITERAL, MacroScriptLexer.DURATION_LITERAL -> Token.LITERAL_NUMBER_DECIMAL_INT;
            case MacroScriptLexer.DECIMAL_LITERAL -> Token.LITERAL_NUMBER_FLOAT;
            case MacroScriptLexer.COLOR_LITERAL -> Token.LITERAL_NUMBER_HEXADECIMAL;
            case MacroScriptLexer.LINE_COMMENT -> Token.COMMENT_EOL;
            case MacroScriptLexer.BLOCK_COMMENT -> Token.COMMENT_MULTILINE;
            case MacroScriptLexer.DOC_COMMENT -> Token.COMMENT_DOCUMENTATION;
            case MacroScriptLexer.UNTERMINATED_COMMENT -> token.getText().startsWith("/**")
                    ? Token.COMMENT_DOCUMENTATION : Token.COMMENT_MULTILINE;
            case MacroScriptLexer.WHITESPACE -> Token.WHITESPACE;
            case MacroScriptLexer.LPAREN, MacroScriptLexer.RPAREN, MacroScriptLexer.LBRACE, MacroScriptLexer.RBRACE,
                 MacroScriptLexer.LBRACKET, MacroScriptLexer.RBRACKET -> Token.SEPARATOR;
            case MacroScriptLexer.ERROR_CHARACTER -> Token.ERROR_CHAR;
            default -> Token.OPERATOR;
        };
    }

    /** A name followed by '(' is a call. An UPPER_CASE name is a constant. */
    private static int identifierType(org.antlr.v4.runtime.Token token, List<org.antlr.v4.runtime.Token> tokens, int index) {
        for (int next = index + 1; next < tokens.size(); next++) {
            int nextType = tokens.get(next).getType();
            if (nextType != MacroScriptLexer.WHITESPACE) {
                if (nextType == MacroScriptLexer.LPAREN) {
                    return Token.FUNCTION;
                }
                break;
            }
        }
        String text = token.getText();
        boolean upperCase = text.chars().anyMatch(Character::isLetter) && text.equals(text.toUpperCase(java.util.Locale.ROOT));
        return upperCase ? CONSTANT : Token.IDENTIFIER;
    }

    /** Adds a string token, with each ${...} part as a token of its own. */
    private void addStringTokens(char[] array, int start, int end, int offset, int stringType) {
        int partStart = start;
        int i = start;
        while (i <= end) {
            if (array[i] == '\\') {
                i += 2;
            } else if (array[i] == '$' && i + 1 <= end && array[i + 1] == '{') {
                int close = closingBrace(array, i + 2, end);
                if (close < 0) {
                    break;
                }
                if (i > partStart) {
                    addToken(array, partStart, i - 1, stringType, offset + (partStart - start));
                }
                addToken(array, i, close, INTERPOLATION, offset + (i - start));
                i = close + 1;
                partStart = i;
            } else {
                i++;
            }
        }
        if (partStart <= end) {
            addToken(array, partStart, end, stringType, offset + (partStart - start));
        }
    }

    private static int closingBrace(char[] array, int from, int end) {
        boolean inQuotes = false;
        for (int i = from; i <= end; i++) {
            if (array[i] == '"') {
                inQuotes = !inQuotes;
            } else if (array[i] == '}' && !inQuotes) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOf(char[] array, int from, int to, String text) {
        outer:
        for (int i = from; i <= to - text.length(); i++) {
            for (int j = 0; j < text.length(); j++) {
                if (array[i + j] != text.charAt(j)) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Override
    public String[] getLineCommentStartAndEnd(int languageIndex) {
        return new String[] {"//", null};
    }

    @Override
    public boolean getCurlyBracesDenoteCodeBlocks(int languageIndex) {
        return true;
    }

    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        return type == Token.IDENTIFIER || type == Token.FUNCTION;
    }

    @Override
    public boolean getShouldIndentNextLineAfter(Token token) {
        if (token != null && token.length() == 1) {
            char c = token.charAt(0);
            return c == '{' || c == '(';
        }
        return false;
    }
}
