package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.JsonSource;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** An object literal, copied to JSON as written. Used for object and rawObject parameters. */
final class ObjectKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.ObjectLiteral)) {
            argument.error("needs an object, for example {\"key\": \"value\"}.");
            return;
        }
        JsonValues.convert(argument.compilation(), argument.value(), true).ifPresent(argument::write);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isObject()) {
            throw context.unrepresentable("expected an object");
        }
        return Optional.of(JsonSource.literal(value));
    }
}
