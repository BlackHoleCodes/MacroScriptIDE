package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** {@code true} or {@code false}. */
final class BoolKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (argument.value() instanceof Expr.BoolLiteral bool) {
            argument.write(argument.json().booleanNode(bool.value()));
        } else {
            argument.error("needs true or false.");
        }
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isBoolean()) {
            throw context.unrepresentable("expected true or false");
        }
        return Optional.of(Boolean.toString(value.booleanValue()));
    }
}
