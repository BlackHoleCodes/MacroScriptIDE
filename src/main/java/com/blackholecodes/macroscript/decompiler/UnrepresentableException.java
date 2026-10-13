package com.blackholecodes.macroscript.decompiler;

/**
 * Thrown inside the decompiler when an item has a value that MacroScript cannot write
 * with normal syntax. The item is then written as {@code raw(...)}, which keeps every field.
 */
public final class UnrepresentableException extends RuntimeException {

    public UnrepresentableException(String reason) {
        super(reason, null, false, false);
    }
}
