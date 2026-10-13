package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.TestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.blackholecodes.macroscript.TestSupport.macro;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The examples of the language reference compile to the output that the reference shows. */
class ReferenceExamplesTest {

    @Test
    void schedulerGivesTheDocumentedOutput() throws Exception {
        String source = """
                @name("WiFi & Bluetooth Scheduler")
                @description("Turns WiFi and Bluetooth on at 7am and off at 11pm, then vibrates")
                triggers {
                    timer(hour: 7, minute: 0, daysOfWeek: EVERY_DAY) as morning;
                    timer(hour: 23, minute: 0, daysOfWeek: EVERY_DAY) as night;
                }
                actions {
                    if (triggeredBy(morning)) {
                        setWifi(TURN_ON);
                        setBluetooth(TURN_ON);
                    } else {
                        setWifi(TURN_OFF);
                        setBluetooth(TURN_OFF);
                    }
                    vibrate(SHORT_BUZZ);
                }
                """;
        String expected = """
                {"macroExportVersion": 1, "macro": {
                  "m_name": "WiFi & Bluetooth Scheduler",
                  "m_description": "Turns WiFi and Bluetooth on at 7am and off at 11pm, then vibrates",
                  "m_category": "Uncategorized", "m_enabled": true, "m_completed": true, "m_GUID": 0, "aiGenerated": 1,
                  "m_triggerList": [
                    {"m_classType": "TimerTrigger", "m_SIGUID": 112233445566771, "m_hour": 7, "m_minute": 0,
                     "m_daysOfWeek": [true, true, true, true, true, true, true]},
                    {"m_classType": "TimerTrigger", "m_SIGUID": 112233445566772, "m_hour": 23, "m_minute": 0,
                     "m_daysOfWeek": [true, true, true, true, true, true, true]}],
                  "m_actionList": [
                    {"m_classType": "IfConditionAction", "m_SIGUID": 0, "m_constraintList": [
                      {"m_classType": "TriggerThatInvokedConstraint", "m_SIGUID": 0,
                       "m_siGuidThatInvoked": 112233445566771, "m_triggerName": "morning"}]},
                    {"m_classType": "SetWifiAction", "m_SIGUID": 0, "m_state": 0},
                    {"m_classType": "SetBluetoothAction", "m_SIGUID": 0, "m_state": 0},
                    {"m_classType": "ElseAction", "m_SIGUID": 0},
                    {"m_classType": "SetWifiAction", "m_SIGUID": 0, "m_state": 1},
                    {"m_classType": "SetBluetoothAction", "m_SIGUID": 0, "m_state": 1},
                    {"m_classType": "EndIfAction", "m_SIGUID": 0},
                    {"m_classType": "VibrateAction", "m_SIGUID": 0, "m_vibratePattern": 1}],
                  "m_constraintList": [],
                  "localVariables": []},
                 "globalVariables": [], "userIcons": null}
                """;
        CompileResult result = TestSupport.compile(source);
        TestSupport.assertNoErrors(result);
        assertEquals(JsonText.parse(expected).toString(), result.output().toString());
    }

    @Test
    void lowBatteryGuardCompiles() {
        JsonNode macro = macro("""
                @name("Low Battery Guard")
                @description("Warns when the battery is low. Turns off WiFi after three warnings.")
                @category("Power")
                variables {
                    int warnings = 0;
                    string lastWarning;
                }
                triggers {
                    batteryLevel(20, decreasesTo: true) as low;
                    externalPower(powerConnected: true) as plugged;
                }
                constraints {
                    airplaneMode(enabled: false);
                }
                actions {
                    if (triggeredBy(plugged)) {
                        warnings = 0;
                        return;
                    }
                    warnings++;
                    lastWarning = "Warning ${warnings} at ${sys.datetime}";
                    /** The text shows the battery level and the last warning. */
                    notify(title: "Battery low", text: "${sys.battery}% left. ${lastWarning}");
                    if (warnings >= 3 && externalPower(externalPower: false)) {
                        setWifi(TURN_OFF);
                    }
                    repeat (3) {
                        vibrate(SHORT_BUZZ);
                        wait(500ms);
                    }
                }
                """);
        JsonNode notify = macro.path("m_actionList").get(6);
        assertEquals("NotificationAction", notify.path("m_classType").asText());
        assertEquals("Battery low", notify.path("m_notificationSubject").asText());
        assertEquals("{battery}% left. {lv=lastWarning}", notify.path("m_notificationText").asText());
        assertEquals("The text shows the battery level and the last warning.", notify.path("m_comment").asText());
        // The fixed fields of ExternalPowerTrigger are always written.
        assertTrue(macro.path("m_triggerList").get(1).path("m_hasSetUSBOption").asBoolean());
    }

    @Test
    void actionBlockIsExportedWithTheMacro() {
        String block = """
                @actionBlock
                @name("Play Sound Effect")
                @description("Plays one sound file from a folder")
                variables {
                    in string soundFile = "beep.mp3";
                    out bool played;
                }
                actions {
                    playSound(staticFilename: false, useAllFilesAccess: true,
                              allFilesPath: "/sdcard/sounds", allFilesFilename: "${soundFile}");
                    played = true;
                }
                """;
        String macro = """
                @description("Calls a block")
                variables {
                    bool ok;
                }
                triggers { manual(); }
                actions {
                    callBlock("Play Sound Effect",
                              inputs: {soundFile: "message.mp3"},
                              outputs: {played: ok});
                    callBlock("Play Sound Effect", async: true);
                }
                """;
        CompileResult result = TestSupport.compile(macro, Map.of("Play Sound Effect", block));
        TestSupport.assertNoErrors(result);
        JsonNode root = result.output().path("macro");
        JsonNode exported = root.path("exportedActionBlocks");
        assertEquals(1, exported.size(), "a block called twice is exported once");
        JsonNode call = root.path("m_actionList").get(0);
        assertEquals("ActionBlockAction", call.path("m_classType").asText());
        assertEquals(exported.get(0).path("m_GUID").asLong(), call.path("actionBlockId").asLong());
        assertEquals("message.mp3", call.path("inputVarsMap").path("soundFile").asText());
        assertEquals("ok", call.path("outputVarsMap").path("played").asText());
        assertTrue(root.path("m_actionList").get(1).path("continueActionsWithoutWaiting").asBoolean());
        assertTrue(exported.get(0).path("isActionBlock").asBoolean());
        assertEquals("{lv=soundFile}", exported.get(0).path("m_actionList").get(0).path("allFilesFilename").asText());
    }

    @Test
    void actionBlockErrorsAreReportedForTheBlockFile() {
        String block = "@actionBlock\n@name(\"Broken\")\n@description(\"x\")\nactions { nope(); }\n";
        CompileResult result = TestSupport.compile("@description(\"x\")\ntriggers { manual(); }\nactions { callBlock(\"Broken\"); }",
                Map.of("Broken", block));
        assertTrue(result.hasErrors());
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.message().contains("has errors")));
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.message().contains("Unknown action 'nope'")));
    }

    @Test
    void wrongBlockInputsAndOutputs() {
        String block = "@actionBlock\n@name(\"B\")\n@description(\"x\")\nvariables { in int n; out string s; }\nactions { s = \"x\"; }\n";
        CompileResult result = TestSupport.compile("""
                @description("x")
                variables { int target; }
                triggers { manual(); }
                actions { callBlock("B", inputs: {m: 1}, outputs: {s: target}); }
                """, Map.of("B", block));
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.message().contains("has no input 'm'")));
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.message().contains("is a string, but 'target' is a int")));
    }
}
