package com.blackholecodes.macroscript.diagnostics;

import java.nio.file.Path;

/**
 * A compile error or warning.
 *
 * @param severity error or warning
 * @param message  the text shown to the user
 * @param range    the source text the message is about
 * @param file     the file the message is about, or null for the file being compiled
 */
public record Diagnostic(Severity severity, String message, SourceRange range, Path file) {

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    /** The same message, attributed to a file. */
    public Diagnostic inFile(Path newFile) {
        return file != null ? this : new Diagnostic(severity, message, range, newFile);
    }

    /** One line of text in the form {@code file:line:column: error: message}. */
    public String format() {
        String where = (file == null ? "" : file.getFileName() + ":") + range.line() + ":" + (range.column() + 1);
        return where + ": " + severity.name().toLowerCase(java.util.Locale.ROOT) + ": " + message;
    }
}
