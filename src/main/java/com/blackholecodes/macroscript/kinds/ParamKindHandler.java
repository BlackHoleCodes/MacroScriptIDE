package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.decompiler.ValueContext;

import java.util.Optional;

/**
 * Converts the value of one kind of parameter between MacroScript and MacroDroid JSON.
 *
 * <p>There is one handler for each {@link com.blackholecodes.macroscript.mapping.ParamKind}.
 * To support a new kind, add the kind to the enum, write a handler, and register it in
 * {@link ParamKinds}.
 */
public interface ParamKindHandler {

    /** Checks the MacroScript value of an argument and writes its JSON into the item. */
    void compile(ArgumentContext argument);

    /**
     * Writes the JSON value of the parameter as MacroScript source. It is called only when
     * the item has a value for the parameter (or, for a parameter with no field of its own,
     * always).
     *
     * @return the source text, or empty when the value is better left out
     * @throws com.blackholecodes.macroscript.decompiler.UnrepresentableException when the
     *         value cannot be written with normal syntax
     */
    Optional<String> decompile(ValueContext value);
}
