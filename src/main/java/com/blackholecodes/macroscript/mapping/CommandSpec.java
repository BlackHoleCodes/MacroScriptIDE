package com.blackholecodes.macroscript.mapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One trigger, constraint or action from the mapping file.
 *
 * @param name         the MacroScript name, for example {@code setWifi}
 * @param classType    the MacroDroid class type that the compiler writes
 * @param category     trigger, constraint or action
 * @param status       how the command can be used
 * @param description  one line of documentation
 * @param syntax       for syntax-only commands, the syntax that produces them, otherwise null
 * @param sugar        a shorter helper that produces the same command, otherwise null
 * @param aliases      other MacroScript names for the command
 * @param paramAliases other argument names, mapped to the JSON field of the real parameter
 * @param params       the parameters, in mapping file order
 * @param notes        extra documentation
 * @param warning      a warning that the compiler gives when the command is used, or null
 */
public record CommandSpec(
        String name,
        String classType,
        CommandCategory category,
        CommandStatus status,
        String description,
        String syntax,
        String sugar,
        List<String> aliases,
        Map<String, String> paramAliases,
        List<ParamSpec> params,
        List<String> notes,
        String warning) {

    /**
     * Finds a parameter by its MacroScript name or by one of its argument aliases.
     */
    public Optional<ParamSpec> param(String argumentName) {
        for (ParamSpec param : params) {
            if (param.name().equals(argumentName)) {
                return Optional.of(param);
            }
        }
        String aliasedField = paramAliases.get(argumentName);
        return aliasedField == null ? Optional.empty() : paramByJsonField(aliasedField);
    }

    public Optional<ParamSpec> paramByJsonField(String jsonField) {
        return params.stream().filter(p -> jsonField.equals(p.jsonField())).findFirst();
    }

    /** The parameter that can be given without a name, if the command has one. */
    public Optional<ParamSpec> positionalParam() {
        return params.stream().filter(ParamSpec::positional).findFirst();
    }

    /** The names of all arguments a user can write, including aliases. */
    public List<String> argumentNames() {
        List<String> names = new java.util.ArrayList<>();
        params.stream().filter(ParamSpec::isWrittenByUser).map(ParamSpec::name).forEach(names::add);
        names.addAll(paramAliases.keySet());
        return names;
    }
}
