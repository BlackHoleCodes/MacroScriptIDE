package com.blackholecodes.macroscript.decompiler;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Writes JSON values as MacroScript literals, for {@code raw(...)} and object parameters. */
public final class JsonSource {

    private JsonSource() {
    }

    public static String literal(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }
        if (node.isTextual()) {
            return StringLiterals.quote(node.asText());
        }
        if (node.isBoolean()) {
            return Boolean.toString(node.booleanValue());
        }
        if (node.isIntegralNumber()) {
            return node.bigIntegerValue().toString();
        }
        if (node.isNumber()) {
            return decimal(node.doubleValue());
        }
        if (node.isArray()) {
            List<String> elements = new ArrayList<>();
            node.forEach(element -> elements.add(literal(element)));
            return "[" + String.join(", ", elements) + "]";
        }
        List<String> entries = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            entries.add(StringLiterals.quote(field.getKey()) + ": " + literal(field.getValue()));
        }
        return entries.isEmpty() ? "{}" : "{" + String.join(", ", entries) + "}";
    }

    /** A decimal literal. It always has a decimal point, so it reads back as a decimal. */
    public static String decimal(double value) {
        String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        return text.contains(".") ? text : text + ".0";
    }
}
