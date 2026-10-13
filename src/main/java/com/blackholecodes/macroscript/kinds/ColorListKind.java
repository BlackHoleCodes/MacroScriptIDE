package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A list of color literals. */
final class ColorListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.ListLiteral list)) {
            argument.error("needs a list of colors, for example [#FF0000, #0000FF].");
            return;
        }
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : list.elements()) {
            if (element instanceof Expr.ColorLiteral color) {
                array.add(color.argb());
            } else {
                argument.error(element, "needs a color here, for example #FF0000.");
                return;
            }
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of colors");
        }
        List<String> colors = new ArrayList<>();
        for (JsonNode color : value) {
            if (!color.isIntegralNumber()) {
                throw context.unrepresentable("expected a color int");
            }
            colors.add(ColorKind.colorLiteral(color.asLong()));
        }
        return Optional.of("[" + String.join(", ", colors) + "]");
    }
}
