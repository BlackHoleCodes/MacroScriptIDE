package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;

import java.util.List;

/**
 * The result of parsing one file.
 *
 * @param file        the syntax tree. It is present also when there are errors, but it can
 *                    then miss the statements that did not parse.
 * @param diagnostics the syntax errors
 */
public record ParseResult(MacroFile file, List<Diagnostic> diagnostics) {

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(Diagnostic::isError);
    }
}
