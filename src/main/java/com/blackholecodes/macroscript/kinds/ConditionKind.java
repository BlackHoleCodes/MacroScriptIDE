package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** A condition expression, compiled to a list of constraints. */
final class ConditionKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        argument.compilation().conditions().compile(argument.value())
                .ifPresent(constraints -> argument.write(constraints.toJson()));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray() || value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(context.decompilation().condition(value));
    }
}
