package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.compiler.DictionaryKeys;
import com.blackholecodes.macroscript.compiler.Reference;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.blackholecodes.macroscript.mapping.VariableEmit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

/**
 * A variable name, optionally with a path into a dict or array: {@code result} or
 * {@code data["a"]["b"]}. Depending on the parameter, the compiler writes the name, a
 * MacroDroidVariable object or a VariableWithDictionaryKeys object, and fills the companion
 * dictionary-path field.
 */
final class VariableKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        Optional<Reference.Variable> resolved = argument.compilation().references().resolveVariable(argument.value());
        if (resolved.isEmpty()) {
            return;
        }
        Reference.Variable variable = resolved.get();
        Optional<List<String>> keys = DictionaryKeys.render(argument.compilation(), variable.keys());
        if (keys.isEmpty()) {
            return;
        }
        ParamSpec param = argument.param();
        VariableEmit emit = param.emit() != null ? param.emit() : VariableEmit.VARIABLE_NAME;

        if (emit == VariableEmit.VARIABLE_WITH_KEYS) {
            ObjectNode node = argument.json().objectNode();
            node.put("variableName", variable.symbol().name());
            if (!keys.get().isEmpty()) {
                node.set("dictionaryKeys", DictionaryKeys.keysObject(keys.get()));
            }
            argument.write(node);
            return;
        }

        JsonNode node = emit == VariableEmit.VARIABLE_OBJECT
                ? variable.symbol().toReference()
                : argument.json().textNode(variable.symbol().name());
        argument.write(node);
        if (keys.get().isEmpty()) {
            return;
        }
        if (param.dictPathField() == null) {
            argument.error("takes a whole variable, not a dict or array entry.");
            return;
        }
        boolean keysObject = argument.command().paramByJsonField(param.dictPathField())
                .map(ParamSpec::usesKeysObject)
                .orElse(true);
        argument.writeCompanion(param.dictPathField(), DictionaryKeys.toJson(keys.get(), keysObject));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (value.isTextual() && value.asText().isEmpty()) {
            return Optional.empty();
        }
        ParamSpec param = context.param();
        VariableEmit emit = param.emit() != null ? param.emit() : VariableEmit.VARIABLE_NAME;
        JsonNode keys = param.dictPathField() == null ? null : context.companion(param.dictPathField());
        return Optional.of(context.decompilation().variable(value, emit, keys));
    }
}
