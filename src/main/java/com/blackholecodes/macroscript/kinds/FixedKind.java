package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.decompiler.ValueContext;

import java.util.Optional;

/** A field that always has the same value. The compiler writes it; the user must not. */
final class FixedKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        argument.error("is set by the compiler. Leave it out.");
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        return Optional.empty();
    }
}
