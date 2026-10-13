package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.TestSupport;
import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.compiler.JsonText;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecompilerTest {

    private static final String FULL_MACRO = """
            @name("Everything")
            @description("Uses most of the language")
            @category("Tests")
            @favourite

            variables {
                int count = 2;
                decimal ratio = 0.5;
                string name = "World";
                bool flag;
                dict scores = {"score": 100, "nested": {"a": true}};
                array names = ["x", "y"];
                color accent;
                secure string apiKey = "abc";
                global int launches;
            }

            triggers {
                timer(hour: 7, minute: 30, daysOfWeek: WEEKDAYS) as morning;
                shakeDevice() when (screenOnOff(screenOn: true));
                /** Runs at night. */
                @disabled
                timer(hour: 23, minute: 0, daysOfWeek: [SAT, SUN]) as night;
            }

            constraints {
                dayOfWeek(daysOfWeek: WEEKDAYS);
                batteryLevel(20, greaterThan: true) || externalPower(true);
            }

            actions {
                if (triggeredBy(morning)) {
                    setWifi(TURN_ON);
                } else if (!triggeredBy(night) && count > 1) {
                    setWifi(TURN_OFF);
                } else {
                    vibrate(SHORT_BUZZ);
                }
                count++;
                count = count * 2 + 1;
                ratio = random(0.0, 1.0);
                name = "Hello ${name}, ${sys.battery}% and ${scores["score"]} and ${launches}";
                accent = #FF2196F3;
                scores["score"] = 5;
                flag = !flag;
                while (count < 10 || flag) {
                    count += 2;
                    if (count == 5) {
                        continue;
                    }
                }
                do {
                    wait(1s500ms);
                } while (sys.battery_int > 50);
                repeat (3) {
                    log("Loop ${sys.loop_iteration}");
                }
                repeat (30s) {
                    break;
                }
                for (key, value : scores) {
                    log("${key} = ${value}");
                    for (key2, value2 : names) {
                        log("${value2}");
                    }
                }
                if (name.containsIgnoreCase("hello") && scores.containsKey("score")) {
                    notify(title: "Hi", text: name);
                }
                if (confirm(title: "Sure?", text: "Really?", yes: "Yes", no: "No")) {
                    log("yes");
                }
                waitFor(shakeDevice(), timeout: 30s, result: flag);
                setWifi(TURN_ON) when ((int) scores["score"] >= 3);
                name = prompt(title: "Name?", message: "Type it");
                raw("SomeFutureAction", {"m_value": 1});
                return;
            }
            """;

    @Test
    void roundTripKeepsTheMacro() {
        CompileResult first = TestSupport.compile(FULL_MACRO);
        TestSupport.assertNoErrors(first);
        DecompileResult decompiled = new Decompiler().decompile(first.output());
        CompileResult second = TestSupport.compile(decompiled.main().source());
        TestSupport.assertNoErrors(second);
        assertEquals(JsonText.pretty(TestSupport.withoutPlaceholderIds(first.output())),
                JsonText.pretty(TestSupport.withoutPlaceholderIds(second.output())),
                "Decompiled source:\n" + decompiled.main().source());
    }

    @Test
    void writesReadableSource() {
        String source = new Decompiler().decompile(TestSupport.compile(FULL_MACRO).output()).main().source();
        assertTrue(source.contains("@name(\"Everything\")"));
        assertTrue(source.contains("global string launches; // check the type"), "a global used only in text has no known type");
        assertTrue(source.contains("timer(hour: 7, minute: 30, daysOfWeek: WEEKDAYS) as morning;"));
        assertTrue(source.contains("} else if (!triggeredBy(night) && count > 1) {"));
        assertTrue(source.contains("    for (key, value : scores) {"));
        assertTrue(source.contains("log(\"${key} = ${value}\");"));
        assertTrue(source.contains("/** Runs at night. */\n    @disabled\n"));
        assertTrue(source.contains("raw(\"SomeFutureAction\", {\"m_value\": 1});"));
    }

    @Test
    void actionBlocksBecomeFilesOfTheirOwn() throws Exception {
        String block = """
                @actionBlock
                @name("Greeter")
                @description("Shows a greeting")
                variables { in string who = "you"; out bool done; }
                actions { toast("Hi ${who}"); done = true; }
                """;
        CompileResult compiled = TestSupport.compile("""
                @name("Uses Block")
                @description("x")
                variables { bool ok; }
                triggers { manual(); }
                actions { callBlock("Greeter", inputs: {who: "Bob"}, outputs: {done: ok}); }
                """, Map.of("Greeter", block));
        TestSupport.assertNoErrors(compiled);
        DecompileResult decompiled = new Decompiler().decompile(compiled.outputText());
        assertEquals(2, decompiled.files().size());
        assertEquals("Uses Block.ms", decompiled.files().get(0).fileName());
        assertEquals("Greeter.ms", decompiled.files().get(1).fileName());
        assertTrue(decompiled.main().source().contains("callBlock(\"Greeter\", inputs: {who: \"Bob\"}, outputs: {done: ok});"));
        String blockSource = decompiled.files().get(1).source();
        assertTrue(blockSource.startsWith("@actionBlock\n"));
        assertTrue(blockSource.contains("in string who = \"you\";"));

        CompileResult again = TestSupport.compile(decompiled.main().source(), Map.of("Greeter", blockSource));
        TestSupport.assertNoErrors(again);
        assertEquals(TestSupport.withoutPlaceholderIds(compiled.output()).toString(),
                TestSupport.withoutPlaceholderIds(again.output()).toString());
    }

    @Test
    void unknownItemsAndUnpairedMarkersBecomeRaw() throws Exception {
        JsonNode export = JsonText.parse("""
                {"macro": {"m_name": "Odd", "m_triggerList": [{"m_classType": "BrandNewTrigger", "m_SIGUID": 5, "x": 1}],
                 "m_actionList": [{"m_classType": "EndIfAction", "m_SIGUID": 0},
                                  {"m_classType": "SetWifiAction", "m_SIGUID": 0, "m_state": 0, "m_unknownField": 7}],
                 "m_constraintList": [], "localVariables": []}}
                """);
        DecompileResult result = new Decompiler().decompile(export);
        String source = result.main().source();
        assertTrue(source.contains("raw(\"BrandNewTrigger\", {\"x\": 1});"));
        assertTrue(source.contains("raw(\"EndIfAction\");"));
        assertTrue(source.contains("raw(\"SetWifiAction\", {\"m_state\": 0, \"m_unknownField\": 7});"));
        assertTrue(!result.warnings().isEmpty());
        TestSupport.assertNoErrors(TestSupport.compile(source));
    }

    @Test
    void rejectsJsonThatIsNotAMacro() {
        assertThrows(IllegalArgumentException.class, () -> new Decompiler().decompile(JsonText.parse("{\"a\": 1}")));
    }
}
