package com.blackholecodes.macroscript.mapping;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One named constant of an enum or flags parameter.
 *
 * @param name  the MacroScript name, for example {@code TURN_ON}
 * @param value the JSON value the compiler writes (an int for most enums, a string for some)
 * @param label the text MacroDroid shows for this value
 */
public record ConstantSpec(String name, JsonNode value, String label) {
}
