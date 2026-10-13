package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.StringLiterals;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HTTP headers or query parameters: {@code {"Accept": "application/json"}}. The compiler
 * writes {@code [{"paramName": ..., "paramValue": ...}]}. Values support magic text.
 */
final class HttpParamsKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.ObjectLiteral object)) {
            argument.error("needs an object of names and values, for example {\"Accept\": \"application/json\"}.");
            return;
        }
        ArrayNode array = argument.json().arrayNode();
        for (Expr.ObjectEntry entry : object.entries()) {
            Optional<String> value = argument.compilation().magicText().textValue(entry.value());
            if (value.isEmpty()) {
                return;
            }
            ObjectNode param = array.addObject();
            param.put("paramName", entry.key());
            param.put("paramValue", value.get());
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of parameters");
        }
        if (value.isEmpty()) {
            return Optional.of("{}");
        }
        List<String> entries = new ArrayList<>();
        for (JsonNode param : value) {
            if (!param.path("paramName").isTextual()) {
                throw context.unrepresentable("expected paramName and paramValue");
            }
            entries.add(StringLiterals.quote(param.path("paramName").asText()) + ": "
                    + context.decompilation().textLiteral(param.path("paramValue").asText("")));
        }
        return Optional.of("{" + String.join(", ", entries) + "}");
    }
}
