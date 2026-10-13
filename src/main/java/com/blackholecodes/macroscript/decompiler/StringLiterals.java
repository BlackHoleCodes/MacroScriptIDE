package com.blackholecodes.macroscript.decompiler;

/** Writes MacroScript string literals. */
public final class StringLiterals {

    private StringLiterals() {
    }

    /**
     * A string literal whose value is exactly {@code text}: quotes, backslashes, line breaks,
     * tabs and {@code ${} are escaped.
     */
    public static String quote(String text) {
        StringBuilder out = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            appendEscaped(out, text, i);
        }
        return out.append('"').toString();
    }

    /** Appends one character of plain text with the escapes a string literal needs. */
    static void appendEscaped(StringBuilder out, String text, int index) {
        char c = text.charAt(index);
        switch (c) {
            case '"' -> out.append("\\\"");
            case '\\' -> out.append("\\\\");
            case '\n' -> out.append("\\n");
            case '\t' -> out.append("\\t");
            case '$' -> out.append(index + 1 < text.length() && text.charAt(index + 1) == '{' ? "\\$" : "$");
            default -> out.append(c);
        }
    }
}
