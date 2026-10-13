package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The path to a dict or array entry, as MacroDroid stores it. Some fields take a plain list
 * of strings, others take an object: {@code {"keys": ["a", "b"]}}.
 */
public final class DictionaryKeys {

    private DictionaryKeys() {
    }

    /** Renders the key expressions of an entry reference as strings. */
    public static Optional<List<String>> render(Compilation compilation, List<Expr> keys) {
        List<String> rendered = new ArrayList<>();
        for (Expr key : keys) {
            Optional<String> text = compilation.magicText().key(key);
            if (text.isEmpty()) {
                return Optional.empty();
            }
            rendered.add(text.get());
        }
        return Optional.of(rendered);
    }

    public static ArrayNode list(List<String> keys) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        keys.forEach(array::add);
        return array;
    }

    /** The {@code {"keys": [...]}} form. */
    public static ObjectNode keysObject(List<String> keys) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("keys", list(keys));
        return node;
    }

    public static JsonNode toJson(List<String> keys, boolean asKeysObject) {
        return asKeysObject ? keysObject(keys) : list(keys);
    }
}
