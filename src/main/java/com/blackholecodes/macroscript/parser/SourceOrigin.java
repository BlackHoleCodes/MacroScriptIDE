package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.diagnostics.SourceRange;
import org.antlr.v4.runtime.Token;

/**
 * Where a piece of parsed text starts in the file. Text inside {@code ${...}} is parsed on
 * its own, so its positions must be moved to the place of the string in the file.
 */
record SourceOrigin(int offset, int line, int column) {

    static final SourceOrigin FILE_START = new SourceOrigin(0, 1, 0);

    SourceRange range(Token start, Token stop) {
        Token last = stop == null || stop.getStopIndex() < start.getStartIndex() ? start : stop;
        int tokenLine = start.getLine();
        return new SourceRange(
                offset + start.getStartIndex(),
                offset + last.getStopIndex() + 1,
                line + tokenLine - 1,
                tokenLine == 1 ? column + start.getCharPositionInLine() : start.getCharPositionInLine());
    }

    SourceRange range(Token token) {
        return range(token, token);
    }

    /** The position of a character inside the text of a token. Tokens never span lines here. */
    SourceOrigin inside(Token token, int charIndex) {
        SourceRange tokenRange = range(token);
        return new SourceOrigin(tokenRange.start() + charIndex, tokenRange.line(), tokenRange.column() + charIndex);
    }
}
