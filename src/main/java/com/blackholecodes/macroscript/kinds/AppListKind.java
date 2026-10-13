package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A list of apps: {@code [app("com.whatsapp", "WhatsApp"), app("org.telegram.messenger")]}.
 * The compiler fills the package list and the parallel name list.
 */
final class AppListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        ArrayNode packages = argument.json().arrayNode();
        ArrayNode names = argument.json().arrayNode();
        for (Expr element : ValueHelpers.elements(argument.value())) {
            Optional<AppKind.App> app = AppKind.read(argument, element);
            if (app.isEmpty()) {
                return;
            }
            packages.add(app.get().packageName());
            names.add(app.get().displayName());
        }
        argument.write(packages);
        if (argument.param().companion() != null) {
            argument.writeCompanion(argument.param().companion(), names);
        }
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode packages = context.value();
        if (!packages.isArray()) {
            throw context.unrepresentable("expected a list of package names");
        }
        JsonNode names = context.param().companion() == null ? null : context.companion(context.param().companion());
        List<String> apps = new ArrayList<>();
        for (int i = 0; i < packages.size(); i++) {
            if (!packages.get(i).isTextual()) {
                throw context.unrepresentable("expected package names");
            }
            JsonNode name = names != null && names.isArray() && i < names.size() ? names.get(i) : null;
            apps.add(AppKind.source(packages.get(i).asText(), name == null || !name.isTextual() ? null : name.asText()));
        }
        return Optional.of("[" + String.join(", ", apps) + "]");
    }
}
