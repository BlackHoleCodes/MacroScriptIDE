package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A list of {@code block(...)} values or {@code null}, one per button. */
final class BlockRefListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.ListLiteral list)) {
            argument.error("needs a list of " + BlockRefKind.EXAMPLE + " or null.");
            return;
        }
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : list.elements()) {
            if (element instanceof Expr.NullLiteral) {
                array.addNull();
            } else if (element instanceof Expr.Call call && call.name().equals("block")) {
                Optional<ObjectNode> data = argument.compilation().actionBlocks().blockData(argument.compilation(), call);
                if (data.isEmpty()) {
                    return;
                }
                array.add(data.get());
            } else {
                argument.error(element, "needs " + BlockRefKind.EXAMPLE + " or null here.");
                return;
            }
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of action block references");
        }
        List<String> elements = new ArrayList<>();
        for (JsonNode element : value) {
            elements.add(element.isNull() ? "null" : context.decompilation().blockReference(element));
        }
        return Optional.of("[" + String.join(", ", elements) + "]");
    }
}
