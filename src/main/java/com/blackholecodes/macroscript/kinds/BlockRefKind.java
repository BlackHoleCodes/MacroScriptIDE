package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * An action block to run from a button or a click: {@code block("Name", inputs: {...},
 * outputs: {...})}. The compiler exports the block and writes an ActionBlockData object.
 */
final class BlockRefKind implements ParamKindHandler {

    static final String EXAMPLE = "block(\"Block Name\", inputs: {...}, outputs: {...})";

    @Override
    public void compile(ArgumentContext argument) {
        if (argument.value() instanceof Expr.Call call && call.name().equals("block")) {
            argument.compilation().actionBlocks().blockData(argument.compilation(), call).ifPresent(argument::write);
        } else {
            argument.error("needs " + EXAMPLE + ".");
        }
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isObject()) {
            throw context.unrepresentable("expected an action block reference");
        }
        return Optional.of(context.decompilation().blockReference(value));
    }
}
