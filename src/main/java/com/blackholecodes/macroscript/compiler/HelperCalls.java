package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.kinds.JsonValues;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The built-in helpers that are shorter forms of mapped commands: {@code wait},
 * {@code waitFor}, {@code confirm} and {@code raw}. {@code callBlock} is in
 * {@link ActionBlockLinker}; {@code random} and {@code prompt} are in {@link AssignmentCompiler}.
 */
public final class HelperCalls {

    /** waitFor argument names and the waitUntilTrigger parameters they set. */
    private static final Map<String, String> WAIT_FOR_ARGUMENTS = Map.of(
            "timeout", "timeoutSeconds",
            "result", "booleanVariableName");

    /** confirm argument names and the ifConfirmedThen parameters they set. */
    private static final Map<String, String> CONFIRM_ARGUMENTS = Map.of(
            "yes", "positiveText",
            "no", "negativeText",
            "timeout", "timeoutSeconds",
            "quitOnBack", "quitOnBackPressed");

    private static final String PAUSE_ACTION = "pause";

    private final Compilation compilation;

    HelperCalls(Compilation compilation) {
        this.compilation = compilation;
    }

    /**
     * {@code raw("ClassName", {fields})}: an item with the given class type and fields,
     * with no checks. It works in all three blocks.
     */
    public Optional<ObjectNode> raw(Expr.Call call) {
        List<Expr.Argument> args = call.arguments();
        if (args.isEmpty() || args.size() > 2 || args.stream().anyMatch(a -> a.name() != null)
                || !(args.get(0).value() instanceof Expr.StringLiteral className) || className.hasInterpolation()
                || (args.size() == 2 && !(args.get(1).value() instanceof Expr.ObjectLiteral))) {
            compilation.error(call.range(), "raw needs a class type and an object: raw(\"SomeAction\", {\"m_field\": 1}).");
            return Optional.empty();
        }
        ObjectNode item = Items.create(className.plainText().orElseThrow());
        if (args.size() == 2) {
            Optional<JsonNode> fields = JsonValues.convert(compilation, args.get(1).value(), true);
            if (fields.isEmpty()) {
                return Optional.empty();
            }
            item.setAll((ObjectNode) fields.get());
        }
        return Optional.of(item);
    }

    /**
     * {@code wait(2s)}, {@code wait(1500ms)}, {@code wait(5)} (seconds) or
     * {@code wait(count, MINUTES)} with an int variable.
     */
    Optional<ObjectNode> wait(Expr.Call call) {
        CommandSpec pause = compilation.mapping().command(CommandCategory.ACTION, PAUSE_ACTION).orElseThrow();
        ObjectNode item = Items.create(pause.classType());
        List<Expr.Argument> args = call.arguments();
        if (args.isEmpty() || args.size() > 2 || args.stream().anyMatch(a -> a.name() != null)) {
            compilation.error(call.range(), "wait needs a duration, for example wait(2s), or a variable and a unit: "
                    + "wait(count, MINUTES).");
            return Optional.empty();
        }
        Expr amount = args.get(0).value();
        switch (amount) {
            case Expr.DurationLiteral duration when args.size() == 1 -> {
                item.put("m_delayInSeconds", duration.millis() / 1000);
                if (duration.millis() % 1000 != 0) {
                    item.put("m_delayInMilliSeconds", duration.millis() % 1000);
                }
            }
            case Expr.IntLiteral seconds when args.size() == 1 -> item.put("m_delayInSeconds", seconds.value());
            default -> {
                if (!waitForVariable(pause, item, amount, args.size() == 2 ? args.get(1).value() : null)) {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(item);
    }

    private boolean waitForVariable(CommandSpec pause, ObjectNode item, Expr variableExpr, Expr unitExpr) {
        Optional<Reference.Variable> variable = compilation.references().resolveVariable(variableExpr);
        if (variable.isEmpty()) {
            return false;
        }
        ParamSpec unitParam = pause.param("unitForVariables").orElseThrow();
        int unit = 0;
        if (unitExpr != null) {
            Optional<ConstantSpec> constant = unitExpr instanceof Expr.Name name ? unitParam.constant(name.name()) : Optional.empty();
            if (constant.isEmpty()) {
                compilation.error(unitExpr.range(), "The unit must be one of: "
                        + String.join(", ", unitParam.constants().stream().map(ConstantSpec::name).toList()) + ".");
                return false;
            }
            unit = constant.get().value().asInt();
        }
        item.set("m_variable", variable.get().symbol().toReference());
        if (variable.get().isEntry()) {
            Optional<List<String>> keys = DictionaryKeys.render(compilation, variable.get().keys());
            if (keys.isEmpty()) {
                return false;
            }
            item.set("varDictionaryKeys", DictionaryKeys.keysObject(keys.get()));
        }
        item.put("unitForVariables", unit);
        return true;
    }

    /** {@code waitFor(trigger(...), timeout: 30s, continueOnTimeout: false, result: ok)}. */
    Optional<ObjectNode> waitFor(Expr.Call call) {
        CommandSpec spec = compilation.mapping().command(CommandCategory.ACTION, "waitUntilTrigger").orElseThrow();
        ObjectNode item = Items.create(spec.classType());
        if (!compilation.commands().writeArguments(spec, rename(call, WAIT_FOR_ARGUMENTS), item)) {
            return Optional.empty();
        }
        if (hasArgument(call, "timeout")) {
            item.put("timeoutEnabled", true);
        }
        return Optional.of(item);
    }

    /**
     * The item that opens an {@code if (confirm(...))} or {@code else if (confirm(...))}
     * branch.
     *
     * @param commandName ifConfirmedThen or elseIfConfirmedThen
     */
    Optional<ObjectNode> confirm(Expr.Call call, String commandName) {
        CommandSpec spec = compilation.mapping().command(CommandCategory.ACTION, commandName).orElseThrow();
        ObjectNode item = Items.create(spec.classType());
        if (!compilation.commands().writeArguments(spec, rename(call, CONFIRM_ARGUMENTS), item)) {
            return Optional.empty();
        }
        if (hasArgument(call, "timeout")) {
            item.put("cancelAfterTimeout", true);
        }
        return Optional.of(item);
    }

    /** A copy of the call with helper argument names changed to the command's names. */
    static Expr.Call rename(Expr.Call call, Map<String, String> renames) {
        List<Expr.Argument> renamed = new ArrayList<>();
        for (Expr.Argument argument : call.arguments()) {
            String name = argument.name() == null ? null : renames.getOrDefault(argument.name(), argument.name());
            renamed.add(new Expr.Argument(name, argument.value(), argument.range()));
        }
        return new Expr.Call(call.name(), List.copyOf(renamed), call.nameRange(), call.range());
    }

    private static boolean hasArgument(Expr.Call call, String name) {
        return call.arguments().stream().anyMatch(a -> name.equals(a.name()));
    }
}
