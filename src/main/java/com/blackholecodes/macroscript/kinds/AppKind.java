package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.StringLiterals;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/**
 * An app: {@code app("com.whatsapp", "WhatsApp")} or a package name string. The compiler
 * fills the package field and its companion name field.
 */
final class AppKind implements ParamKindHandler {

    static final String EXAMPLE = "app(\"com.whatsapp\", \"WhatsApp\")";

    @Override
    public void compile(ArgumentContext argument) {
        read(argument, argument.value()).ifPresent(app -> {
            argument.write(argument.json().textNode(app.packageName()));
            if (argument.param().companion() != null) {
                argument.writeCompanion(argument.param().companion(), argument.json().textNode(app.displayName()));
            }
        });
    }

    /** A package name and the name to show for it. */
    record App(String packageName, String displayName) {
    }

    static Optional<App> read(ArgumentContext argument, Expr value) {
        if (value instanceof Expr.StringLiteral literal && !literal.hasInterpolation()) {
            String packageName = literal.plainText().orElseThrow();
            return Optional.of(new App(packageName, packageName));
        }
        if (value instanceof Expr.Call call && call.name().equals("app")) {
            Optional<List<String>> strings = ValueHelpers.strings(argument, call, 2, EXAMPLE);
            return strings.map(s -> new App(s.get(0), s.size() > 1 ? s.get(1) : s.get(0)));
        }
        argument.error(value, "needs an app, for example " + EXAMPLE + ".");
        return Optional.empty();
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        JsonNode name = context.param().companion() == null ? null : context.companion(context.param().companion());
        return Optional.of(source(value.asText(), name == null || name.isNull() ? null : name.asText()));
    }

    /** {@code app("pkg")} or {@code app("pkg", "Name")}. */
    static String source(String packageName, String displayName) {
        if (displayName == null || displayName.equals(packageName)) {
            return "app(" + StringLiterals.quote(packageName) + ")";
        }
        return "app(" + StringLiterals.quote(packageName) + ", " + StringLiterals.quote(displayName) + ")";
    }
}
