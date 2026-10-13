package com.blackholecodes.macroscript.compiler;

import java.nio.file.Path;

/**
 * The text of one {@code .ms} file.
 *
 * @param path the file, or null for text that is not saved to a file
 * @param text the source code
 */
public record SourceUnit(Path path, String text) {

    /** The file name without {@code .ms}, used as the macro name when there is no @name. */
    public String baseName() {
        if (path == null) {
            return "Untitled";
        }
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".ms") ? fileName.substring(0, fileName.length() - 3) : fileName;
    }
}
