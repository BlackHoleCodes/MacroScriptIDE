package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.StringLiterals;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** A string literal with no magic text. Also used for app display names. */
final class StringKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.StringLiteral literal)) {
            argument.error("needs a string, for example \"text\".");
            return;
        }
        if (literal.hasInterpolation()) {
            argument.error("does not support ${...} magic text. Write plain text.");
            return;
        }
        argument.write(argument.json().textNode(literal.plainText().orElseThrow()));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isTextual()) {
            throw context.unrepresentable("expected text");
        }
        return Optional.of(StringLiterals.quote(value.asText()));
    }
}
