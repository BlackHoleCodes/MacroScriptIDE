package com.blackholecodes.macroscript;

import com.blackholecodes.macroscript.compiler.ActionBlockResolver;
import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.compiler.Compiler;
import com.blackholecodes.macroscript.compiler.SourceUnit;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Helpers shared by the tests. */
public final class TestSupport {

    private static final Compiler COMPILER = new Compiler();

    private TestSupport() {
    }

    public static CompileResult compile(String source) {
        return COMPILER.compile(source);
    }

    /** Compiles with action blocks given as name to source text. */
    public static CompileResult compile(String source, Map<String, String> blocks) {
        ActionBlockResolver resolver = name -> Optional.ofNullable(blocks.get(name)).map(text -> new SourceUnit(null, text));
        return COMPILER.compile(new SourceUnit(null, source), resolver);
    }

    /** Compiles and fails the test when there is an error. Returns the macro object. */
    public static JsonNode macro(String source) {
        CompileResult result = compile(source);
        assertNoErrors(result);
        return result.output().path("macro");
    }

    /** A complete macro around some actions, with the given variables. */
    public static String actions(String variables, String actions) {
        return "@description(\"test\")\nvariables {\n" + variables + "\n}\ntriggers { manual(); }\nactions {\n" + actions + "\n}\n";
    }

    /** The action list of a macro made by {@link #actions}. */
    public static JsonNode actionList(String variables, String actions) {
        return macro(actions(variables, actions)).path("m_actionList");
    }

    public static void assertNoErrors(CompileResult result) {
        if (result.hasErrors()) {
            fail("Unexpected errors:\n" + describe(result));
        }
    }

    /** Checks that compiling gives an error whose message contains the text. */
    public static void assertError(String source, String messagePart) {
        CompileResult result = compile(source);
        assertTrue(result.hasErrors(), "Expected an error containing '" + messagePart + "'");
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.isError() && d.message().contains(messagePart)),
                "No error contains '" + messagePart + "'. Errors:\n" + describe(result));
        assertFalse(result.output() != null, "A failed compilation must not give output");
    }

    public static String describe(CompileResult result) {
        return result.diagnostics().stream().map(Diagnostic::format).collect(Collectors.joining("\n"));
    }

    /**
     * A copy of an export with every placeholder ID replaced by 0. Placeholder IDs depend on
     * the order labels are given out, which a round trip need not keep.
     */
    public static JsonNode withoutPlaceholderIds(JsonNode node) {
        JsonNode copy = node.deepCopy();
        clearIds(copy);
        return copy;
    }

    private static void clearIds(JsonNode node) {
        if (node instanceof ObjectNode object) {
            for (String field : new String[] {"m_SIGUID", "m_siGuidThatInvoked", "m_GUID", "actionBlockId"}) {
                if (object.has(field)) {
                    object.put(field, 0);
                }
            }
        }
        node.forEach(TestSupport::clearIds);
    }
}
