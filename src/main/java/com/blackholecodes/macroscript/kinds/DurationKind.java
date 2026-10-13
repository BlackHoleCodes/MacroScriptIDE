package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.DurationUnit;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * A duration literal such as {@code 30s} or {@code 1m30s}, converted to the unit of the
 * parameter. A plain int is taken to be in the unit of the parameter.
 */
final class DurationKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        DurationUnit unit = unitOf(argument);
        switch (argument.value()) {
            case Expr.DurationLiteral duration -> {
                if (duration.millis() % unit.millis() != 0) {
                    argument.error("is stored in whole " + unitName(unit) + ", so this duration cannot be written exactly.");
                } else {
                    argument.write(JsonValues.number(duration.millis() / unit.millis()));
                }
            }
            case Expr.IntLiteral number -> argument.write(JsonValues.number(number.value()));
            default -> argument.error("needs a duration, for example 30s, 5m or 1h30m.");
        }
    }

    static DurationUnit unitOf(ArgumentContext argument) {
        DurationUnit unit = argument.param().unit();
        return unit != null ? unit : DurationUnit.SECONDS;
    }

    private static String unitName(DurationUnit unit) {
        return unit.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isIntegralNumber()) {
            throw context.unrepresentable("expected a whole number");
        }
        DurationUnit unit = context.param().unit() != null ? context.param().unit() : DurationUnit.SECONDS;
        long amount = value.asLong();
        if (amount < 0) {
            return Optional.of(Long.toString(amount));
        }
        return Optional.of(amount + unit.suffix());
    }
}
