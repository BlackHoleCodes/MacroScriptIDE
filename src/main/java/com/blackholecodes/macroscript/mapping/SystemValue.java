package com.blackholecodes.macroscript.mapping;

/**
 * A MacroDroid system value, written {@code sys.name} in MacroScript.
 *
 * @param name      the MacroScript name after {@code sys.}
 * @param magicText the MacroDroid magic text, for example {@code {battery}}
 * @param source    the group or trigger that provides the value
 */
public record SystemValue(String name, String magicText, String source) {
}
