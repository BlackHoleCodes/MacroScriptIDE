package com.blackholecodes.macroscript.diagnostics;

/**
 * A span of source text.
 *
 * @param start  offset of the first character (0-based)
 * @param end    offset after the last character
 * @param line   line of the first character (1-based)
 * @param column column of the first character (0-based)
 */
public record SourceRange(int start, int end, int line, int column) {

    /** A range for things that have no place in the source, such as a missing block. */
    public static final SourceRange NONE = new SourceRange(0, 0, 1, 0);

    public int length() {
        return Math.max(0, end - start);
    }

    /** A range that covers this range and the other one. */
    public SourceRange to(SourceRange other) {
        return start <= other.start
                ? new SourceRange(start, Math.max(end, other.end), line, column)
                : other.to(this);
    }
}
