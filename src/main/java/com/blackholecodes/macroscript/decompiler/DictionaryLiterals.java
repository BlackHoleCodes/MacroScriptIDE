package com.blackholecodes.macroscript.decompiler;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/** Writes the {@code dictionary} value of a dict or array variable as a MacroScript literal. */
final class DictionaryLiterals {

    private DictionaryLiterals() {
    }

    /** {@code {"a": 1}} or {@code [1, 2]} for {@code {"entries": [...], "isArray": ...}}. */
    static String literal(JsonNode dictionary) {
        boolean isArray = dictionary.path("isArray").asBoolean();
        List<String> parts = new ArrayList<>();
        for (JsonNode entry : dictionary.path("entries")) {
            String value = value(entry.path("variable"));
            parts.add(isArray ? value : StringLiterals.quote(entry.path("key").asText()) + ": " + value);
        }
        return isArray ? "[" + String.join(", ", parts) + "]" : "{" + String.join(", ", parts) + "}";
    }

    private static String value(JsonNode variable) {
        return switch (variable.path("type").asText()) {
            case "StringValue" -> StringLiterals.quote(variable.path("textValue").asText());
            case "IntegerValue" -> Long.toString(variable.path("intValue").asLong());
            case "DecimalValue" -> JsonSource.decimal(variable.path("decimalValue").asDouble());
            case "BooleanValue" -> Boolean.toString(variable.path("booleanValue").asBoolean());
            case "Dictionary" -> literal(variable);
            default -> throw new UnrepresentableException("Unknown dictionary entry type " + variable.path("type").asText());
        };
    }
}
