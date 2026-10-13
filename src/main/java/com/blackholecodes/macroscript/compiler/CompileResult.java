package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * The result of compiling one file.
 *
 * @param output      the MacroDroid export JSON, or null when there are errors
 * @param diagnostics the errors and warnings, for this file and for the action blocks it calls
 */
public record CompileResult(ObjectNode output, List<Diagnostic> diagnostics) {

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(Diagnostic::isError);
    }

    /** The output as indented JSON text. */
    public String outputText() {
        if (output == null) {
            throw new IllegalStateException("The compilation has errors, so there is no output.");
        }
        return JsonText.pretty(output);
    }
}
