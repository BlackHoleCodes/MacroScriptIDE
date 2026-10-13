package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.JsonSource;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Optional;

/** A list of object literals. */
final class ObjectListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : ValueHelpers.elements(argument.value())) {
            if (!(element instanceof Expr.ObjectLiteral)) {
                argument.error(element, "needs a list of objects, for example [{\"name\": \"a\"}].");
                return;
            }
            Optional<JsonNode> converted = JsonValues.convert(argument.compilation(), element, true);
            if (converted.isEmpty()) {
                return;
            }
            array.add(converted.get());
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of objects");
        }
        for (JsonNode element : value) {
            if (!element.isObject()) {
                throw context.unrepresentable("expected objects");
            }
        }
        return Optional.of(JsonSource.literal(value));
    }
}
