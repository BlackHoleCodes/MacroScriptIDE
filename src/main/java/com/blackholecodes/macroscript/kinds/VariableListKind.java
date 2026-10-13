package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.compiler.Reference;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A list of variable names. The compiler writes the names as strings. */
final class VariableListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        Expr value = argument.value();
        List<Expr> names = value instanceof Expr.ListLiteral list ? list.elements() : List.of(value);
        ArrayNode array = argument.json().arrayNode();
        for (Expr name : names) {
            if (name instanceof Expr.NullLiteral) {
                array.addNull();
                continue;
            }
            Optional<Reference.Variable> variable = argument.compilation().references().resolveVariable(name);
            if (variable.isEmpty()) {
                return;
            }
            array.add(variable.get().symbol().name());
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of variable names");
        }
        List<String> names = new ArrayList<>();
        for (JsonNode name : value) {
            if (name.isNull()) {
                names.add("null");
            } else if (name.isTextual()) {
                names.add(context.decompilation().variable(name, com.blackholecodes.macroscript.mapping.VariableEmit.VARIABLE_NAME, null));
            } else {
                throw context.unrepresentable("expected variable names");
            }
        }
        return Optional.of("[" + String.join(", ", names) + "]");
    }
}
