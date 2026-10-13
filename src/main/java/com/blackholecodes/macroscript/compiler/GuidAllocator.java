package com.blackholecodes.macroscript.compiler;

/**
 * Gives out the placeholder IDs that MacroDroid replaces at import: 11223344556677
 * followed by a counter (112233445566771, 112233445566772, ...). Labeled triggers and
 * exported action blocks use them. Items that nothing refers to get 0.
 */
public final class GuidAllocator {

    private static final String PREFIX = "11223344556677";

    private int counter;

    public long next() {
        counter++;
        return Long.parseLong(PREFIX + counter);
    }
}
