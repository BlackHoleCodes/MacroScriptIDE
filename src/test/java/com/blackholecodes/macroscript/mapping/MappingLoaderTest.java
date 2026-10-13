package com.blackholecodes.macroscript.mapping;

import com.blackholecodes.macroscript.kinds.ParamKinds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingLoaderTest {

    private final Mapping mapping = MappingLoader.loadDefault();

    @Test
    void loadsEveryCommand() {
        assertEquals(124, mapping.commands(CommandCategory.TRIGGER).size());
        assertEquals(87, mapping.commands(CommandCategory.CONSTRAINT).size());
        assertEquals(208, mapping.commands(CommandCategory.ACTION).size());
    }

    @Test
    void everyCommandHasAClassTypeAndEveryKindHasAHandler() {
        for (CommandCategory category : CommandCategory.values()) {
            for (CommandSpec command : mapping.commands(category)) {
                assertFalse(command.classType().isBlank(), command.name());
                for (ParamSpec param : command.params()) {
                    assertNotNull(ParamKinds.instance().handler(param.kind()), param.kind().name());
                }
            }
        }
    }

    @Test
    void findsCommandsByNameAliasAndClassType() {
        assertEquals("SetWifiAction", mapping.command(CommandCategory.ACTION, "setWifi").orElseThrow().classType());
        assertEquals("NotificationAction", mapping.command(CommandCategory.ACTION, "notify").orElseThrow().classType());
        assertEquals("EmptyTrigger", mapping.command(CommandCategory.TRIGGER, "manual").orElseThrow().classType());
        assertEquals("setWifi", mapping.commandByClassType(CommandCategory.ACTION, "SetWifiAction").orElseThrow().name());
    }

    @Test
    void aliasCommandsWriteTheClassTypeTheyStandFor() {
        CommandSpec confirmDialog = mapping.command(CommandCategory.ACTION, "confirmDialog").orElseThrow();
        assertEquals("OptionDialogAction", confirmDialog.classType());
        // The callable command keeps the class type for decompiling.
        assertEquals("optionDialog", mapping.commandByClassType(CommandCategory.ACTION, "OptionDialogAction").orElseThrow().name());
    }

    @Test
    void readsLanguageData() {
        assertEquals("{battery}", mapping.systemValue("battery").orElseThrow().magicText());
        assertEquals(5, mapping.dayGroup("WEEKDAYS").orElseThrow().size());
        assertTrue(mapping.reservedWords().contains("while"));
    }
}
