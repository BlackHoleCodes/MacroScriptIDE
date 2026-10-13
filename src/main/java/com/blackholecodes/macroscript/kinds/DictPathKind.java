package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.compiler.DictionaryKeys;
import com.blackholecodes.macroscript.decompiler.StringLiterals;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The path into a dict or array. It is normally filled from the variable argument
 * ({@code data["a"]}), but a list of keys can also be given.
 */
final class DictPathKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        if (!(argument.value() instanceof Expr.ListLiteral list)) {
            argument.error("needs a list of keys, for example [\"a\", \"b\"].");
            return;
        }
        List<String> keys = new ArrayList<>();
        for (Expr key : list.elements()) {
            Optional<String> text = argument.compilation().magicText().key(key);
            if (text.isEmpty()) {
                return;
            }
            keys.add(text.get());
        }
        argument.write(DictionaryKeys.toJson(keys, argument.param().usesKeysObject()));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        JsonNode keys = value.isObject() ? value.path("keys") : value;
        if (!keys.isArray() || keys.isEmpty()) {
            return Optional.empty();
        }
        List<String> quoted = new ArrayList<>();
        keys.forEach(key -> quoted.add(StringLiterals.quote(key.asText())));
        return Optional.of("[" + String.join(", ", quoted) + "]");
    }
}
