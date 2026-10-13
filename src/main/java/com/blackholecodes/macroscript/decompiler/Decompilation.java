package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.VariableEmit;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The state of the decompilation of one macro, and the parts of the decompiler that work on
 * it. The parameter kind handlers use the public methods.
 */
public final class Decompilation {

    private final Mapping mapping;
    private final List<String> warnings;
    private final VariableRegistry variables = new VariableRegistry();
    private final LoopAliases loops = new LoopAliases();
    private final Map<Long, String> triggerLabels = new HashMap<>();

    private final MagicTextReader magicText = new MagicTextReader(this);
    private final ArithmeticReader arithmetic = new ArithmeticReader(magicText);
    private final ItemDecompiler items = new ItemDecompiler(this);
    private final ConditionDecompiler conditions = new ConditionDecompiler(this);

    Decompilation(Mapping mapping, List<String> warnings) {
        this.mapping = mapping;
        this.warnings = warnings;
    }

    public Mapping mapping() {
        return mapping;
    }

    /** A MacroScript string literal for a magic text value. */
    public String textLiteral(String magicTextValue) {
        return magicText.stringLiteral(magicTextValue);
    }

    /**
     * A variable argument, for example {@code data["a"]}.
     *
     * @param value the field value: a name, a MacroDroidVariable object or a
     *              VariableWithDictionaryKeys object
     * @param keys  the dictionary path field ({@code {"keys": [...]}} or a list), or null
     */
    public String variable(JsonNode value, VariableEmit emit, JsonNode keys) {
        String name;
        JsonNode path = keys;
        if (emit == VariableEmit.VARIABLE_OBJECT) {
            name = variables.register(value);
        } else if (emit == VariableEmit.VARIABLE_WITH_KEYS) {
            name = variables.registerName(value.path("variableName").asText());
            path = value.get("dictionaryKeys");
        } else {
            name = variables.registerName(value.asText());
        }
        return name + keySuffix(path);
    }

    /** {@code ["a"][0]} for a dictionary path, or an empty string for none. */
    public String keySuffix(JsonNode keys) {
        if (keys == null || keys.isNull()) {
            return "";
        }
        JsonNode list = keys.isObject() ? keys.path("keys") : keys;
        StringBuilder suffix = new StringBuilder();
        for (JsonNode key : list) {
            Optional<String> source = magicText.keySource(key.asText());
            suffix.append('[').append(source.orElseThrow(
                    () -> new UnrepresentableException("The dictionary key " + key + " cannot be written."))).append(']');
        }
        return suffix.toString();
    }

    /** A trigger call for a trigger nested in an action, such as the triggers of waitFor. */
    public String triggerCall(JsonNode trigger) {
        return items.call(trigger, CommandCategory.TRIGGER);
    }

    /** A condition for a list of constraint items, joined with AND. */
    public String condition(JsonNode constraints) {
        return conditions.list(constraints, false);
    }

    /** {@code block("Name", inputs: {...}, outputs: {...})} for an ActionBlockData object. */
    public String blockReference(JsonNode data) {
        return "block(" + ActionBlockCalls.arguments(this, data) + ")";
    }

    public void warn(String message) {
        warnings.add(message);
    }

    // ----- for the decompiler classes ----------------------------------------------------

    VariableRegistry variables() {
        return variables;
    }

    LoopAliases loops() {
        return loops;
    }

    MagicTextReader magicText() {
        return magicText;
    }

    ArithmeticReader arithmetic() {
        return arithmetic;
    }

    ItemDecompiler items() {
        return items;
    }

    ConditionDecompiler conditions() {
        return conditions;
    }

    Map<Long, String> triggerLabels() {
        return triggerLabels;
    }

    List<String> warnings() {
        return new ArrayList<>(warnings);
    }
}
