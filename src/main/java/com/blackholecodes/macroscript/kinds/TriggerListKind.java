package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One trigger call or a list of them, nested inside an action (for example waitFor). */
final class TriggerListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : ValueHelpers.elements(argument.value())) {
            if (!(element instanceof Expr.Call call)) {
                argument.error(element, "needs a trigger call, for example shakeDevice().");
                return;
            }
            Optional<ObjectNode> trigger = argument.compilation().triggers().compileCall(call);
            if (trigger.isEmpty()) {
                return;
            }
            array.add(trigger.get());
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray() || value.isEmpty()) {
            return Optional.empty();
        }
        List<String> triggers = new ArrayList<>();
        value.forEach(trigger -> triggers.add(context.decompilation().triggerCall(trigger)));
        return Optional.of(triggers.size() == 1 ? triggers.get(0) : "[" + String.join(", ", triggers) + "]");
    }
}
