package com.blackholecodes.macroscript.build;

import com.blackholecodes.macroscript.compiler.CompileResult;

import java.nio.file.Path;

/**
 * The result of building one file.
 *
 * @param source     the {@code .ms} file
 * @param outputFile the JSON file that was written, or null when there were errors
 * @param compile    the compiler result with the errors and warnings
 */
public record BuildResult(Path source, Path outputFile, CompileResult compile) {

    public boolean succeeded() {
        return outputFile != null;
    }
}
