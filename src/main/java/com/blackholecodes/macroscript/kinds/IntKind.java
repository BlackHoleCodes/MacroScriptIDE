package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.IntRange;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** A whole number. The mapping can give a permitted range, for example 0-23. */
final class IntKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.IntLiteral number)) {
            argument.error("needs a whole number, for example 5.");
            return;
        }
        IntRange range = argument.param().range();
        if (range != null && !range.contains(number.value())) {
            argument.error("must be in the range " + range + ".");
            return;
        }
        argument.write(JsonValues.number(number.value()));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isIntegralNumber()) {
            throw context.unrepresentable("expected a whole number");
        }
        return Optional.of(value.asText());
    }
}
