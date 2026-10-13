package com.blackholecodes.macroscript.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.blackholecodes.macroscript.TestSupport.actionList;
import static com.blackholecodes.macroscript.TestSupport.macro;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per row of the language reference tables. */
class LanguageFeaturesTest {

    private static final String VARIABLES = """
            bool b; bool other; int i; int j; decimal x; string s; string name;
            dict d = {"k": 1, "inner": {"a": "b"}, "list": [1, 2]};
            array a = ["hello", "world"];
            color c;
            global int launches;
            """;

    private static JsonNode firstAction(String statement) {
        return actionList(VARIABLES, statement).get(0);
    }

    @Nested
    class Variables {

        @Test
        void writesLocalVariablesWithTheirInitialValues() {
            JsonNode macro = macro("""
                    @description("test")
                    variables {
                        int count = 3;
                        string a, b = "x";
                        decimal ratio = 0.5;
                        secure string key = "abc";
                        global int launches;
                        color accent;
                    }
                    triggers { manual(); }
                    actions { count++; }
                    """);
            JsonNode locals = macro.path("localVariables");
            assertEquals(6, locals.size(), "the global variable is not written");
            assertEquals(3, locals.get(0).path("m_intValue").asInt());
            assertEquals(1, locals.get(0).path("m_type").asInt());
            assertEquals("", locals.get(1).path("m_stringValue").asText());
            assertEquals("x", locals.get(2).path("m_stringValue").asText());
            assertEquals(0.5, locals.get(3).path("m_decimalValue").asDouble());
            assertTrue(locals.get(4).path("isSecure").asBoolean());
            assertTrue(locals.get(0).path("isLocalVar").asBoolean());
        }

        @Test
        void writesDictAndArrayLiterals() {
            JsonNode macro = macro("""
                    @description("test")
                    variables { dict scores = {"score": 100, "nested": {"a": true}}; array names = ["x", 1.5]; }
                    triggers { manual(); }
                    actions { log("x"); }
                    """);
            JsonNode scores = macro.path("localVariables").get(0).path("dictionary");
            assertFalse(scores.path("isArray").asBoolean());
            assertEquals("score", scores.path("entries").get(0).path("key").asText());
            assertEquals("IntegerValue", scores.path("entries").get(0).path("variable").path("type").asText());
            assertEquals(100, scores.path("entries").get(0).path("variable").path("intValue").asInt());
            JsonNode nested = scores.path("entries").get(1).path("variable");
            assertEquals("Dictionary", nested.path("type").asText());
            assertTrue(nested.path("entries").get(0).path("variable").path("booleanValue").asBoolean());

            JsonNode names = macro.path("localVariables").get(1).path("dictionary");
            assertTrue(names.path("isArray").asBoolean());
            assertEquals("0", names.path("entries").get(0).path("key").asText());
            assertEquals("DecimalValue", names.path("entries").get(1).path("variable").path("type").asText());
        }

        @Test
        void actionBlockVariablesSupportInputAndOutput() {
            JsonNode macro = macro("""
                    @actionBlock
                    @description("test")
                    variables { in string file = "a.mp3"; out bool ok; }
                    actions { ok = true; }
                    """);
            assertTrue(macro.path("isActionBlock").asBoolean());
            assertTrue(macro.path("localVariables").get(0).path("supportsInput").asBoolean());
            assertTrue(macro.path("localVariables").get(1).path("supportsOutput").asBoolean());
        }
    }

    @Nested
    class Assignments {

        @Test
        void boolAssignments() {
            assertTrue(firstAction("b = true;").path("m_newBooleanValue").asBoolean());
            assertTrue(firstAction("b = !b;").path("m_booleanInvert").asBoolean());
            assertEquals("other", firstAction("b = other;").path("m_otherBooleanVariable").path("m_name").asText());
        }

        @Test
        void intAssignments() {
            JsonNode set = firstAction("i = 5;");
            assertEquals("SetVariableAction", set.path("m_classType").asText());
            assertEquals("i", set.path("m_variable").path("m_name").asText());
            assertEquals(1, set.path("m_variable").path("m_type").asInt());
            assertEquals(5, set.path("m_newIntValue").asInt());
            assertTrue(firstAction("i++;").path("m_intValueIncrement").asBoolean());
            assertTrue(firstAction("i += 1;").path("m_intValueIncrement").asBoolean());
            assertTrue(firstAction("i--;").path("m_intValueDecrement").asBoolean());
            assertEquals("{lv=i}+3", firstAction("i += 3;").path("m_expression").asText());
            assertEquals("{lv=j}*2+1", firstAction("i = j * 2 + 1;").path("m_expression").asText());
            assertEquals("{lv=i}*({lv=j}+1)", firstAction("i *= j + 1;").path("m_expression").asText());
            assertEquals("{v=launches}-1", firstAction("i = launches - 1;").path("m_expression").asText());
            JsonNode random = firstAction("i = random(1, 10);");
            assertTrue(random.path("m_intRandom").asBoolean());
            assertEquals(1, random.path("m_intRandomMin").asInt());
            assertEquals(10, random.path("m_intRandomMax").asInt());
        }

        @Test
        void decimalAssignments() {
            assertEquals(1.5, firstAction("x = 1.5;").path("m_newDoubleValue").asDouble());
            JsonNode random = firstAction("x = random(0.0, 1.0);");
            assertTrue(random.path("m_intRandom").asBoolean());
            assertEquals(1.0, random.path("m_doubleRandomMax").asDouble());
        }

        @Test
        void stringAssignments() {
            assertEquals("Hi {lv=name}", firstAction("s = \"Hi ${name}\";").path("m_newStringValue").asText());
            assertEquals("{lv=s}!", firstAction("s += \"!\";").path("m_newStringValue").asText());
            assertEquals("a{lv=name}b", firstAction("s = \"a\" + name + \"b\";").path("m_newStringValue").asText());
            assertEquals("{clipboard}", firstAction("s = sys.clipboard;").path("m_newStringValue").asText());
            JsonNode prompt = firstAction("s = prompt(title: \"Name?\", message: \"Type it\");");
            assertTrue(prompt.path("m_userPrompt").asBoolean());
            assertEquals("Name?", prompt.path("m_userPromptTitle").asText());
            assertEquals("Type it", prompt.path("m_userPromptMessage").asText());
        }

        @Test
        void colorAssignments() {
            assertEquals("#FF0000", firstAction("c = #FF0000;").path("m_newStringValue").asText());
        }

        @Test
        void dictionaryEntries() {
            JsonNode set = firstAction("d[\"k\"] = 5;");
            assertEquals("k", set.path("dictionaryKeys").get(0).asText());
            assertEquals(1, set.path("dictionaryOrArrayType").asInt());
            assertEquals(5, set.path("m_newIntValue").asInt());
            assertEquals("0", firstAction("a[0] = \"x\";").path("dictionaryKeys").get(0).asText());
            assertEquals("{lv=s}", firstAction("d[s] = 5;").path("dictionaryKeys").get(0).asText());
            assertEquals("a", firstAction("d = a;").path("copyDictionaryLocation").path("variableName").asText());
        }
    }

    @Nested
    class Conditions {

        private JsonNode condition(String condition) {
            return firstAction("if (" + condition + ") { log(\"x\"); }").path("m_constraintList");
        }

        private JsonNode single(String condition) {
            JsonNode list = condition(condition);
            assertEquals(1, list.size(), "one constraint for " + condition);
            return list.get(0);
        }

        @Test
        void boolVariables() {
            assertTrue(single("b").path("m_booleanValue").asBoolean());
            assertFalse(single("!b").path("m_booleanValue").asBoolean());
            assertEquals("MacroDroidVariableConstraint", single("b").path("m_classType").asText());
        }

        @Test
        void intComparisons() {
            assertEquals(5, single("i == 5").path("m_intValue").asInt());
            assertTrue(single("i != 5").path("m_intNotEqual").asBoolean());
            assertTrue(single("i > 5").path("m_intGreaterThan").asBoolean());
            assertTrue(single("i < 5").path("m_intLessThan").asBoolean());
            JsonNode atLeast = single("i >= 5");
            assertTrue(atLeast.path("m_intGreaterThan").asBoolean());
            assertEquals(4, atLeast.path("m_intValue").asInt());
            JsonNode compareVariable = single("i > j");
            assertTrue(compareVariable.path("m_intCompareVariable").asBoolean());
            assertEquals("j", compareVariable.path("m_otherValueToCompare").path("m_name").asText());
            assertEquals("{lv=j}*2", single("i > (j * 2)").path("m_expression").asText());
            assertTrue(single("5 < i").path("m_intGreaterThan").asBoolean(), "a literal on the left is swapped");
        }

        @Test
        void decimalComparisons() {
            JsonNode greater = single("x > 1.5");
            assertEquals(1.5, greater.path("m_doubleValue").asDouble());
            assertTrue(greater.path("m_intGreaterThan").asBoolean());
            JsonNode atLeast = single("x >= 1.5");
            assertEquals("LogicConstraint", atLeast.path("m_classType").asText());
            assertEquals(1, atLeast.path("m_option").asInt());
            assertEquals(2, atLeast.path("m_childConstraints").size());
        }

        @Test
        void stringComparisons() {
            JsonNode equal = single("s == \"a\"");
            assertEquals("a", equal.path("m_stringValue").asText());
            assertEquals(0, equal.path("m_stringComparisonType").asInt());
            assertTrue(equal.path("m_stringEqual").asBoolean());
            assertTrue(equal.path("checkCase").asBoolean());
            assertFalse(single("s != \"a\"").path("m_stringEqual").asBoolean());
            assertFalse(single("s.equalsIgnoreCase(\"a\")").path("checkCase").asBoolean());
            assertEquals(1, single("s.contains(\"a\")").path("m_stringComparisonType").asInt());
            assertFalse(single("s.containsIgnoreCase(\"a\")").path("checkCase").asBoolean());
            assertEquals(2, single("!s.contains(\"a\")").path("m_stringComparisonType").asInt());
            assertTrue(single("s.matches(\"^a.*\")").path("enableRegex").asBoolean());
        }

        @Test
        void dictionaryEntries() {
            JsonNode entry = single("d[\"k\"] == 5");
            assertEquals("k", entry.path("dictionaryKeys").path("keys").get(0).asText());
            assertEquals(1, entry.path("dictionaryType").asInt());
            assertEquals(1, single("(int) d[\"k\"] > i").path("dictionaryType").asInt());
        }

        @Test
        void sysValuesAndText() {
            JsonNode battery = single("sys.battery_int > 50");
            assertEquals("CompareValueConstraint", battery.path("m_classType").asText());
            assertEquals(1, battery.path("type").asInt());
            assertEquals("{battery_int}", battery.path("value1").asText());
            assertEquals("50", battery.path("value2").asText());
            assertEquals(3, battery.path("comparisonType").asInt());
            JsonNode ssid = single("\"${sys.ssid}\".contains(\"Home\")");
            assertEquals(2, ssid.path("type").asInt());
            assertEquals(4, ssid.path("comparisonType").asInt());
        }

        @Test
        void collections() {
            JsonNode key = single("d.containsKey(\"k\")");
            assertEquals(4, key.path("type").asInt());
            assertEquals("{lv=d}", key.path("value1").asText());
            assertEquals("k", key.path("value2").asText());
            assertEquals(6, key.path("comparisonType").asInt());
            assertEquals(5, single("a.containsValue(\"x\")").path("type").asInt());
            assertEquals(9, single("!a.containsValue(\"x\")").path("comparisonType").asInt());
        }

        @Test
        void logic() {
            JsonNode and = condition("b && i > 5");
            assertEquals(2, and.size());
            JsonNode macroIf = firstAction("if (b || i > 5) { log(\"x\"); }");
            assertTrue(macroIf.path("m_isOrCondition").asBoolean());
            JsonNode mixed = condition("b && (i > 5 || s == \"a\")");
            assertEquals("LogicConstraint", mixed.get(1).path("m_classType").asText());
            assertEquals(1, mixed.get(1).path("m_option").asInt());
            JsonNode not = single("!screenOnOff(screenOn: true)");
            assertEquals(3, not.path("m_option").asInt());
            assertEquals("ScreenOnOffConstraint", not.path("m_childConstraints").get(0).path("m_classType").asText());
            assertEquals(2, single("xor(b, i > 5)").path("m_option").asInt());
        }
    }

    @Nested
    class ControlFlow {

        @Test
        void ifElseIfElse() {
            JsonNode list = actionList(VARIABLES, """
                    if (b) { log("1"); } else if (i > 1) { log("2"); } else { log("3"); }
                    """);
            assertEquals(java.util.List.of("IfConditionAction", "LogAction", "ElseIfConditionAction", "LogAction",
                    "ElseAction", "LogAction", "EndIfAction"), classTypes(list));
        }

        @Test
        void confirm() {
            JsonNode list = actionList(VARIABLES, """
                    if (confirm(title: "Sure?", text: "Really?", yes: "Yes", timeout: 10s)) { log("y"); } else { log("n"); }
                    """);
            JsonNode confirm = list.get(0);
            assertEquals("IfConfirmedThenAction", confirm.path("m_classType").asText());
            assertEquals("Yes", confirm.path("positiveText").asText());
            assertTrue(confirm.path("cancelAfterTimeout").asBoolean());
            assertEquals(10, confirm.path("timeoutSeconds").asInt());
        }

        @Test
        void loops() {
            JsonNode list = actionList(VARIABLES, """
                    while (i < 10) { i++; }
                    do { wait(5s); } while (b);
                    while (true) { break; }
                    repeat (5) { continue; }
                    repeat (i) { log("x"); }
                    repeat (2m) { log("x"); }
                    """);
            assertEquals(1, list.get(0).path("m_option").asInt());
            assertEquals(1, list.get(0).path("m_constraintList").size());
            assertEquals(2, list.get(3).path("m_option").asInt());
            assertEquals(3, list.get(6).path("m_option").asInt());
            assertEquals("BreakFromLoopAction", list.get(7).path("m_classType").asText());
            assertEquals(5, list.get(9).path("m_fixedOptionCount").asInt());
            assertEquals("ContinueLoopAction", list.get(10).path("m_classType").asText());
            assertEquals("i", list.get(12).path("m_fixedOptionVariable").path("m_name").asText());
            JsonNode timed = list.get(15);
            assertEquals(4, timed.path("m_option").asInt());
            assertEquals(2, timed.path("timedDurationValue").asInt());
            assertEquals(1, timed.path("timedTimeUnit").asInt());
        }

        @Test
        void forLoopAliasesBecomeIteratorMagicText() {
            JsonNode list = actionList(VARIABLES, """
                    for (key, value : d) { log("${key} = ${value}"); }
                    for (index, item : a) { log("${index}: ${item}"); }
                    for (k, v : d["inner"]) { log("${v}"); }
                    """);
            JsonNode iterate = list.get(0);
            assertEquals("IterateDictionaryAction", iterate.path("m_classType").asText());
            assertEquals("d", iterate.path("variableName").asText());
            assertFalse(iterate.path("isArray").asBoolean());
            assertEquals(0, iterate.path("dictionaryKeys").path("keys").size());
            assertEquals("{iterator_dictionary_key} = {iterator_value}", list.get(1).path("m_logText").asText());
            assertEquals("EndLoopAction", list.get(2).path("m_classType").asText());
            assertTrue(list.get(3).path("isArray").asBoolean());
            assertEquals("{iterator_array_index}: {iterator_value}", list.get(4).path("m_logText").asText());
            assertEquals("inner", list.get(6).path("dictionaryKeys").path("keys").get(0).asText());
        }

        @Test
        void returnStopsTheMacro() {
            assertEquals("TerminateRunningAction", firstAction("return;").path("m_classType").asText());
        }
    }

    @Nested
    class Helpers {

        @Test
        void waitWritesSecondsAndMilliseconds() {
            JsonNode pause = firstAction("wait(1500ms);");
            assertEquals("PauseAction", pause.path("m_classType").asText());
            assertEquals(1, pause.path("m_delayInSeconds").asInt());
            assertEquals(500, pause.path("m_delayInMilliSeconds").asInt());
            JsonNode variable = firstAction("wait(i, MINUTES);");
            assertEquals("i", variable.path("m_variable").path("m_name").asText());
            assertEquals(1, variable.path("unitForVariables").asInt());
        }

        @Test
        void waitForNestsTheTrigger() {
            JsonNode wait = firstAction("waitFor(shakeDevice(), timeout: 30s, result: b);");
            assertEquals("WaitUntilTriggerAction", wait.path("m_classType").asText());
            assertEquals("ShakeDeviceTrigger", wait.path("triggersToWaitFor").get(0).path("m_classType").asText());
            assertEquals(30, wait.path("timeoutSeconds").asInt());
            assertTrue(wait.path("timeoutEnabled").asBoolean());
            assertEquals("b", wait.path("booleanVariableName").asText());
        }

        @Test
        void rawCopiesTheItem() {
            JsonNode raw = firstAction("raw(\"SomeNewAction\", {\"m_value\": 3, \"m_text\": \"${s}\"});");
            assertEquals("SomeNewAction", raw.path("m_classType").asText());
            assertEquals(3, raw.path("m_value").asInt());
            assertEquals("{lv=s}", raw.path("m_text").asText());
        }

        @Test
        void contactAndApp() {
            JsonNode call = firstAction("makeCall(contact: contact(\"Mom\", \"+1555\"));");
            assertEquals("Mom", call.path("m_contact").path("m_name").asText());
            assertEquals("-1", call.path("m_contact").path("m_id").asText());
            JsonNode launch = firstAction("launchApp(LAUNCH_BY_APP_CHOOSER, packageToLaunch: app(\"com.whatsapp\", \"WhatsApp\"));");
            assertEquals("LaunchActivityAction", launch.path("m_classType").asText());
            assertTrue(launch.toString().contains("com.whatsapp"));
            assertTrue(launch.toString().contains("WhatsApp"));
        }
    }

    @Nested
    class StringsAndMagicText {

        @Test
        void interpolation() {
            assertEquals("{lv=d[k]} {v=launches} {lv=d[inner][a]} {lv=a[0]} {lv=d[{lv=s}]} {battery}",
                    firstAction("log(\"${d[\"k\"]} ${launches} ${d[\"inner\"][\"a\"]} ${a[0]} ${d[s]} ${sys.battery}\");")
                            .path("m_logText").asText());
        }

        @Test
        void escapes() {
            assertEquals("${x} \"q\" \\ \n", firstAction("log(\"\\${x} \\\"q\\\" \\\\ \\n\");").path("m_logText").asText());
            assertEquals("\\{battery} and {notMagic}", firstAction("log(\"\\{battery} and \\{notMagic}\");")
                    .path("m_logText").asText());
            assertEquals("{battery}", firstAction("log(\"{battery}\");").path("m_logText").asText(),
                    "text in single braces is passed on unchanged");
        }

        @Test
        void bareVariableInTextArgument() {
            assertEquals("{lv=name}", firstAction("log(name);").path("m_logText").asText());
        }
    }

    @Nested
    class Statements {

        @Test
        void docCommentsAndAnnotationsSetItemFields() {
            JsonNode list = actionList(VARIABLES, """
                    /** Turns WiFi off. */
                    setWifi(TURN_OFF);
                    @disabled
                    @comment("Off for now")
                    setBluetooth(TURN_OFF);
                    """);
            assertEquals("Turns WiFi off.", list.get(0).path("m_comment").asText());
            assertTrue(list.get(1).path("m_isDisabled").asBoolean());
            assertEquals("Off for now", list.get(1).path("m_comment").asText());
        }

        @Test
        void whenAddsConstraintsToOneAction() {
            JsonNode action = firstAction("setWifi(TURN_ON) when (b || i > 2);");
            assertEquals(2, action.path("m_constraintList").size());
            assertTrue(action.path("m_isOrCondition").asBoolean());
        }

        @Test
        void triggersWithLabelsAndWhen() {
            JsonNode macro = macro("""
                    @description("test")
                    triggers {
                        timer(hour: 7, minute: 30, daysOfWeek: WEEKDAYS) as morning;
                        shakeDevice() when (screenOnOff(screenOn: true));
                    }
                    actions { if (!triggeredBy(morning)) { log("shake"); } }
                    """);
            JsonNode timer = macro.path("m_triggerList").get(0);
            assertEquals(112233445566771L, timer.path("m_SIGUID").asLong());
            assertEquals("[true,true,true,true,true,false,false]", timer.path("m_daysOfWeek").toString());
            assertEquals("ScreenOnOffConstraint", macro.path("m_triggerList").get(1).path("m_constraintList").get(0)
                    .path("m_classType").asText());
            JsonNode invoked = macro.path("m_actionList").get(0).path("m_constraintList").get(0);
            assertEquals(112233445566771L, invoked.path("m_siGuidThatInvoked").asLong());
            assertEquals("morning", invoked.path("m_triggerName").asText());
            assertTrue(invoked.path("m_not").asBoolean());
        }

        @Test
        void dayOfWeekConstraintIsSundayFirst() {
            JsonNode macro = macro("""
                    @description("test")
                    triggers { manual(); }
                    constraints { dayOfWeek(daysOfWeek: [SUN, MON]); }
                    actions { log("x"); }
                    """);
            assertEquals("[true,true,false,false,false,false,false]",
                    macro.path("m_constraintList").get(0).path("m_daysOfWeek").toString());
        }

        @Test
        void constraintsBlockWithOneFlatOrSetsTheMacroOrFlag() {
            JsonNode macro = macro("""
                    @description("test")
                    triggers { manual(); }
                    constraints { airplaneMode(enabled: true) || screenOnOff(screenOn: true); }
                    actions { log("x"); }
                    """);
            assertTrue(macro.path("m_isOrCondition").asBoolean());
            assertEquals(2, macro.path("m_constraintList").size());
        }

        @Test
        void annotationsSetMacroFields() {
            JsonNode macro = macro("""
                    @name("Named")
                    @description("Does things")
                    @category("Tools")
                    @enabled(false)
                    @favourite
                    @headingColor(#FFFF0000)
                    @noLog
                    @logLevel(TRIGGERS_ONLY)
                    triggers { manual(); }
                    actions { log("x"); }
                    """);
            assertEquals("Named", macro.path("m_name").asText());
            assertEquals("Tools", macro.path("m_category").asText());
            assertFalse(macro.path("m_enabled").asBoolean());
            assertTrue(macro.path("isFavourite").asBoolean());
            assertEquals(-65536, macro.path("m_headingColor").asInt());
            assertTrue(macro.path("m_excludeLog").asBoolean());
            assertEquals(1, macro.path("loggingLevel").asInt());
            assertEquals(0, macro.path("m_GUID").asInt());
            assertEquals(1, macro.path("aiGenerated").asInt());
        }
    }

    private static java.util.List<String> classTypes(JsonNode list) {
        java.util.List<String> types = new java.util.ArrayList<>();
        list.forEach(item -> types.add(item.path("m_classType").asText()));
        return types;
    }
}
