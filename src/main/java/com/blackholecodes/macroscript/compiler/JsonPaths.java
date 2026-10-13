package com.blackholecodes.macroscript.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Reads and writes fields at dotted paths such as {@code requestConfig.urlToOpen}. */
public final class JsonPaths {

    private JsonPaths() {
    }

    public static void put(ObjectNode root, String path, JsonNode value) {
        String[] parts = path.split("\\.");
        ObjectNode parent = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = parent.get(parts[i]);
            parent = child instanceof ObjectNode childObject ? childObject : parent.putObject(parts[i]);
        }
        parent.set(parts[parts.length - 1], value);
    }

    /** The value at the path, or null when any part of the path is missing. */
    public static JsonNode get(JsonNode root, String path) {
        JsonNode node = root;
        for (String part : path.split("\\.")) {
            if (node == null || !node.isObject()) {
                return null;
            }
            node = node.get(part);
        }
        return node;
    }

    /** Removes the field at the path, if it is there. */
    public static void remove(ObjectNode root, String path) {
        int dot = path.lastIndexOf('.');
        JsonNode parent = dot < 0 ? root : get(root, path.substring(0, dot));
        if (parent instanceof ObjectNode parentObject) {
            parentObject.remove(path.substring(dot + 1));
        }
    }

    /** The path of a field that sits next to the field at {@code siblingPath}. */
    public static String sibling(String siblingPath, String field) {
        int dot = siblingPath.lastIndexOf('.');
        return dot < 0 ? field : siblingPath.substring(0, dot + 1) + field;
    }
}
