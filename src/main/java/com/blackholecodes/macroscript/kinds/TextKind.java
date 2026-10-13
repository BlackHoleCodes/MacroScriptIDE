package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Text that supports magic text: a string literal with {@code ${...}} parts, or a bare
 * variable name, which becomes {@code ${name}}.
 */
final class TextKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        argument.compilation().magicText().textValue(argument.value())
                .ifPresent(text -> argument.write(argument.json().textNode(text)));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isTextual()) {
            throw context.unrepresentable("expected text");
        }
        return Optional.of(context.decompilation().textLiteral(value.asText()));
    }
}
