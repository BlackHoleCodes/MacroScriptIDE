package com.blackholecodes.macroscript.compiler;

/**
 * The aliases of one {@code for (key, value : source)} loop.
 *
 * @param isArray true when the loop walks an array, so the key is the index
 */
public record LoopScope(String keyAlias, String valueAlias, boolean isArray) {

    /** The magic text MacroDroid uses for the key of the innermost loop. */
    public String keyMagicText() {
        return isArray ? "{iterator_array_index}" : "{iterator_dictionary_key}";
    }

    public static String valueMagicText() {
        return "{iterator_value}";
    }
}
