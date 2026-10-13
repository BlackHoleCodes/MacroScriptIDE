package com.blackholecodes.macroscript.mapping;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The lookup table that the compiler, the decompiler and the editor share. It holds every
 * command from {@code macroscript-mapping.json} plus the language data the file defines.
 *
 * <p>Use {@link MappingLoader#loadDefault()} to get the copy that ships with the program.
 */
public final class Mapping {

    private final String macroScriptVersion;
    private final String macroDroidVersion;
    private final Map<CommandCategory, Map<String, CommandSpec>> commandsByName = new EnumMap<>(CommandCategory.class);
    private final Map<CommandCategory, Map<String, CommandSpec>> commandsByClassType = new EnumMap<>(CommandCategory.class);
    private final Map<CommandCategory, List<CommandSpec>> commandLists = new EnumMap<>(CommandCategory.class);
    private final Map<String, SystemValue> systemValuesByName = new LinkedHashMap<>();
    private final Map<String, SystemValue> systemValuesByMagicText = new LinkedHashMap<>();
    private final Map<String, List<String>> dayGroups;
    private final List<String> reservedWords;

    Mapping(String macroScriptVersion,
            String macroDroidVersion,
            Map<CommandCategory, List<CommandSpec>> commands,
            List<SystemValue> systemValues,
            Map<String, List<String>> dayGroups,
            List<String> reservedWords) {
        this.macroScriptVersion = macroScriptVersion;
        this.macroDroidVersion = macroDroidVersion;
        this.dayGroups = Map.copyOf(dayGroups);
        this.reservedWords = List.copyOf(reservedWords);
        for (CommandCategory category : CommandCategory.values()) {
            List<CommandSpec> list = List.copyOf(commands.getOrDefault(category, List.of()));
            Map<String, CommandSpec> byName = new LinkedHashMap<>();
            Map<String, CommandSpec> byClassType = new LinkedHashMap<>();
            for (CommandSpec command : list) {
                byName.put(command.name(), command);
                command.aliases().forEach(alias -> byName.putIfAbsent(alias, command));
                // When two commands share a class type (an alias command), the callable one wins.
                byClassType.merge(command.classType(), command,
                        (existing, added) -> existing.status() == CommandStatus.ALIAS ? added : existing);
            }
            commandLists.put(category, list);
            commandsByName.put(category, byName);
            commandsByClassType.put(category, byClassType);
        }
        for (SystemValue value : systemValues) {
            systemValuesByName.put(value.name(), value);
            systemValuesByMagicText.putIfAbsent(value.magicText(), value);
        }
    }

    /** Finds a command by its MacroScript name or one of its aliases. */
    public Optional<CommandSpec> command(CommandCategory category, String name) {
        return Optional.ofNullable(commandsByName.get(category).get(name));
    }

    /** Finds the command that writes the given MacroDroid class type. */
    public Optional<CommandSpec> commandByClassType(CommandCategory category, String classType) {
        return Optional.ofNullable(commandsByClassType.get(category).get(classType));
    }

    /** Finds a command by class type in any category. */
    public Optional<CommandSpec> commandByClassType(String classType) {
        for (CommandCategory category : CommandCategory.values()) {
            Optional<CommandSpec> found = commandByClassType(category, classType);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public List<CommandSpec> commands(CommandCategory category) {
        return commandLists.get(category);
    }

    /** All names (including aliases) that call a command of the category. */
    public Collection<String> commandNames(CommandCategory category) {
        return commandsByName.get(category).keySet();
    }

    public Optional<SystemValue> systemValue(String name) {
        return Optional.ofNullable(systemValuesByName.get(name));
    }

    /** Finds a system value by its magic text, for example {@code {battery}}. */
    public Optional<SystemValue> systemValueByMagicText(String magicText) {
        return Optional.ofNullable(systemValuesByMagicText.get(magicText));
    }

    public Collection<SystemValue> systemValues() {
        return systemValuesByName.values();
    }

    /** The days in a day group such as WEEKDAYS, or empty when the name is not a group. */
    public Optional<List<String>> dayGroup(String name) {
        return Optional.ofNullable(dayGroups.get(name));
    }

    public Map<String, List<String>> dayGroups() {
        return dayGroups;
    }

    public List<String> reservedWords() {
        return reservedWords;
    }

    public String macroScriptVersion() {
        return macroScriptVersion;
    }

    public String macroDroidVersion() {
        return macroDroidVersion;
    }
}
