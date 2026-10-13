package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.JsonSource;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** A decimal number. An int literal is also accepted. */
final class DecimalKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        switch (argument.value()) {
            case Expr.DecimalLiteral d -> argument.write(argument.json().numberNode(d.value()));
            case Expr.IntLiteral i -> argument.write(argument.json().numberNode((double) i.value()));
            default -> argument.error("needs a number, for example 1.5.");
        }
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isNumber()) {
            throw context.unrepresentable("expected a number");
        }
        return Optional.of(JsonSource.decimal(value.doubleValue()));
    }
}
