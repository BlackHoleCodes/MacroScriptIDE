package com.blackholecodes.macroscript.compiler;

import org.junit.jupiter.api.Test;

import static com.blackholecodes.macroscript.TestSupport.actions;
import static com.blackholecodes.macroscript.TestSupport.assertError;
import static com.blackholecodes.macroscript.TestSupport.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The compiler explains mistakes instead of writing a broken macro. */
class CompileErrorsTest {

    @Test
    void syntaxErrorsHaveAPosition() {
        var result = compile("@description(\"x\")\ntriggers { manual(); }\nactions {\n    setWifi(TURN_ON)\n}\n");
        assertTrue(result.hasErrors());
        assertEquals(5, result.diagnostics().get(0).range().line());
    }

    @Test
    void unterminatedStringsAndComments() {
        assertError(actions("", "log(\"oops);"), "no closing quote");
        assertError(actions("", "/* never closed"), "no closing */");
    }

    @Test
    void blocks() {
        assertError("@description(\"x\")\nactions { log(\"x\"); }", "needs a triggers block");
        assertError("@description(\"x\")\ntriggers { manual(); }", "needs an actions block");
        assertError("@description(\"x\")\ntriggers { }\nactions { log(\"x\"); }", "at least one trigger");
        assertError("@actionBlock\n@description(\"x\")\ntriggers { manual(); }\nactions { log(\"x\"); }",
                "cannot have a triggers block");
        assertError("@description(\"x\")\ntriggers { manual(); }\ntriggers { manual(); }\nactions { log(\"x\"); }",
                "only one triggers block");
    }

    @Test
    void unknownCommandsAndArgumentsSuggestNames() {
        assertError(actions("", "setWfi(TURN_ON);"), "Did you mean 'setWifi'?");
        assertError(actions("", "setWifi(stat: TURN_ON);"), "Did you mean 'state'?");
        assertError(actions("", "setWifi(TURN_ONN);"), "Did you mean 'TURN_ON'?");
        assertError(actions("", "setWifi();"), "needs the argument 'state'");
        assertError(actions("", "setWifi(TURN_ON, TURN_OFF);"), "Only one argument can be written without a name");
        assertError(actions("", "setWifi(state: TURN_ON, state: TURN_OFF);"), "given twice");
    }

    @Test
    void commandsInTheWrongBlock() {
        assertError(actions("", "shakeDevice();"), "is a trigger, not an action");
        assertError(actions("", "if (shakeDevice()) { log(\"x\"); }"), "A condition needs a constraint");
        assertError(actions("", "ifCondition();"), "cannot be called");
    }

    @Test
    void argumentValues() {
        assertError(actions("", "setWifi(state: true);"), "needs one of");
        assertError("@description(\"x\")\ntriggers { timer(hour: 25, minute: 0, daysOfWeek: WEEKDAYS); }\nactions { log(\"x\"); }",
                "range 0-23");
        assertError(actions("string s;", "setWifi(TURN_ON, ssid: \"${s}\");"), "does not support ${...}");
        assertError(actions("", "log(\"${missing}\");"), "'missing' is not declared");
        assertError(actions("", "log(\"${sys.batery}\");"), "Did you mean 'battery'?");
    }

    @Test
    void variables() {
        assertError(actions("global int g = 5;", "log(\"x\");"), "global variable cannot have an initial value");
        assertError(actions("color c = #FF0000;", "log(\"x\");"), "color variable cannot have an initial value");
        assertError(actions("in string s;", "log(\"x\");"), "in and out are for action blocks");
        assertError(actions("int a; int a;", "log(\"x\");"), "declared twice");
        assertError(actions("int a = \"x\";", "log(\"x\");"), "must be a whole number");
        assertError(actions("", "x = 5;"), "'x' is not declared");
        assertError(actions("int i;", "i = \"text\";"), "is a int, but this value is a string");
    }

    @Test
    void conditions() {
        assertError(actions("string s;", "if (s > \"a\") { log(\"x\"); }"), "only be compared with == and !=");
        assertError(actions("int i;", "if (i) { log(\"x\"); }"), "cannot be a condition on its own");
        assertError(actions("", "if (triggeredBy(nope)) { log(\"x\"); }"), "No trigger has the label 'nope'");
        assertError(actions("", "confirm(text: \"x\");"), "confirm() is a condition");
        assertError(actions("", "if (false) { log(\"x\"); }"), "cannot be the literal false");
    }

    @Test
    void loops() {
        assertError(actions("", "break;"), "only be used inside a loop");
        assertError(actions("int i;", "for (k, v : i) { log(\"x\"); }"), "needs a dict or array");
        assertError(actions("dict d; dict e;", "for (k, v : d) { for (k2, v2 : e) { log(\"${v}\"); } }"),
                "belongs to an outer for loop");
    }

    @Test
    void annotations() {
        assertError("@nmae(\"x\")\ntriggers { manual(); }\nactions { log(\"x\"); }", "Did you mean 'name'?");
        assertError(actions("", "@favourite\nlog(\"x\");"), "not permitted on a statement");
    }

    @Test
    void missingDescriptionIsAWarning() {
        var result = compile("triggers { manual(); }\nactions { log(\"x\"); }");
        assertTrue(!result.hasErrors() && result.diagnostics().stream().anyMatch(d -> d.message().contains("@description")));
    }

    @Test
    void actionBlocks() {
        assertError(actions("", "callBlock(\"Nowhere\");"), "No action block is named 'Nowhere'");
    }
}
