package com.blackholecodes.macroscript.decompiler;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Writes the arguments of {@code callBlock(...)} and {@code block(...)}. */
final class ActionBlockCalls {

    private ActionBlockCalls() {
    }

    /** {@code "Name", inputs: {...}, outputs: {...}} for an ActionBlockAction or ActionBlockData. */
    static String arguments(Decompilation decompilation, JsonNode data) {
        if (!data.path("actionBlockName").isTextual()) {
            throw new UnrepresentableException("An action block call with no block name");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(StringLiterals.quote(data.path("actionBlockName").asText()));
        String inputs = inputs(decompilation, data.path("inputVarsMap"), data.path("inputDictionaryMap"));
        if (!inputs.isEmpty()) {
            arguments.add("inputs: " + inputs);
        }
        String outputs = variables(decompilation, data.path("outputVarsMap"), data.path("outputDictionaryMap"));
        if (!outputs.isEmpty()) {
            arguments.add("outputs: " + outputs);
        }
        return String.join(", ", arguments);
    }

    /** Inputs are magic text, except dict and array inputs, which name a variable. */
    private static String inputs(Decompilation decompilation, JsonNode values, JsonNode dictionaries) {
        List<String> entries = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = values.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String value = field.getValue().asText();
            boolean namesVariable = dictionaries.has(field.getKey())
                    || (decompilation.variables().type(value).map(t -> t.isCollection()).orElse(false));
            String source = namesVariable
                    ? decompilation.variables().registerName(value) + decompilation.keySuffix(dictionaries.get(field.getKey()))
                    : decompilation.textLiteral(value);
            entries.add(field.getKey() + ": " + source);
        }
        return entries.isEmpty() ? "" : "{" + String.join(", ", entries) + "}";
    }

    private static String variables(Decompilation decompilation, JsonNode values, JsonNode dictionaries) {
        List<String> entries = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = values.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String name = decompilation.variables().registerName(field.getValue().asText());
            entries.add(field.getKey() + ": " + name + decompilation.keySuffix(dictionaries.get(field.getKey())));
        }
        return entries.isEmpty() ? "" : "{" + String.join(", ", entries) + "}";
    }
}
