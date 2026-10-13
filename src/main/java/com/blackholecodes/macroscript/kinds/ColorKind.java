package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** A color literal ({@code #FF0000} or {@code #AARRGGBB}) or an int. Written as a signed ARGB int. */
final class ColorKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        switch (argument.value()) {
            case Expr.ColorLiteral color -> argument.write(argument.json().numberNode(color.argb()));
            case Expr.IntLiteral number -> argument.write(JsonValues.number(number.value()));
            default -> argument.error("needs a color, for example #FF2196F3.");
        }
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isIntegralNumber()) {
            throw context.unrepresentable("expected a color int");
        }
        return Optional.of(colorLiteral(value.asLong()));
    }

    /** {@code #AARRGGBB} for a signed ARGB int. */
    static String colorLiteral(long argb) {
        return String.format("#%08X", argb & 0xFFFFFFFFL);
    }
}
