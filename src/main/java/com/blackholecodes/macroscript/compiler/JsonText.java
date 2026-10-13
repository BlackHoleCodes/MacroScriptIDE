package com.blackholecodes.macroscript.compiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/** Reads and writes JSON text. */
public final class JsonText {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private JsonText() {
    }

    public static String pretty(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node) + System.lineSeparator();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("A JSON tree could not be written", e);
        }
    }

    /** Parses JSON text. Throws {@link JsonProcessingException} when the text is not valid JSON. */
    public static JsonNode parse(String text) throws JsonProcessingException {
        return MAPPER.readTree(text);
    }
}
