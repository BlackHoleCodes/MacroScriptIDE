package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.JsonSource;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A list literal. A single value is taken as a list of one. */
final class ListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        boolean magicText = argument.param().interpolation();
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : ValueHelpers.elements(argument.value())) {
            Optional<JsonNode> converted = JsonValues.convert(argument.compilation(), element, magicText);
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
            throw context.unrepresentable("expected a list");
        }
        boolean magicText = context.param().interpolation();
        List<String> elements = new ArrayList<>();
        for (JsonNode element : value) {
            elements.add(magicText && element.isTextual()
                    ? context.decompilation().textLiteral(element.asText())
                    : JsonSource.literal(element));
        }
        return Optional.of("[" + String.join(", ", elements) + "]");
    }
}
