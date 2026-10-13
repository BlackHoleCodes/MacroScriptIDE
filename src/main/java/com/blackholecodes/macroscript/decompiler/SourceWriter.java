package com.blackholecodes.macroscript.decompiler;

/** Builds source text line by line with four-space indentation. */
final class SourceWriter {

    private static final String INDENT = "    ";

    private final StringBuilder text = new StringBuilder();
    private int depth;

    SourceWriter line(String line) {
        text.append(INDENT.repeat(depth)).append(line).append('\n');
        return this;
    }

    SourceWriter blankLine() {
        text.append('\n');
        return this;
    }

    /** Writes a line that ends with an opening brace, and indents the lines after it. */
    SourceWriter open(String line) {
        line(line);
        depth++;
        return this;
    }

    /** Ends an indented part with a line such as "}" or "} else {". */
    SourceWriter close(String line) {
        depth = Math.max(0, depth - 1);
        return line(line);
    }

    /** Ends an indented part and starts the next one, as for "} else {". */
    SourceWriter reopen(String line) {
        close(line);
        depth++;
        return this;
    }

    /** Adds text that was written by another writer, indented to the current depth. */
    SourceWriter append(SourceWriter other) {
        for (String line : other.toString().split("\n", -1)) {
            if (!line.isEmpty()) {
                line(line);
            }
        }
        return this;
    }

    boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
