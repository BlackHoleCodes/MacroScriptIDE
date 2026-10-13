package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.kinds.ArgumentContext;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.CommandStatus;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamKind;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Compiles a call of a mapped command, for example {@code setWifi(TURN_ON)}, into one
 * MacroDroid item. The mapping file decides the class type, the argument names, which
 * arguments are required and the JSON field of each one.
 */
public final class CommandEmitter {

    private final Compilation compilation;

    CommandEmitter(Compilation compilation) {
        this.compilation = compilation;
    }

    /** Finds the command for a call and reports an error when there is none. */
    public Optional<CommandSpec> lookup(CommandCategory category, Expr.Call call) {
        Optional<CommandSpec> spec = compilation.mapping().command(category, call.name());
        if (spec.isEmpty()) {
            compilation.error(call.nameRange(), unknownCommandMessage(category, call.name()));
            return Optional.empty();
        }
        CommandSpec command = spec.get();
        if (command.status() == CommandStatus.SYNTAX) {
            compilation.error(call.nameRange(), "'" + call.name() + "' cannot be called. Write it with the syntax "
                    + command.syntax() + ".");
            return Optional.empty();
        }
        if (command.status() == CommandStatus.DEPRECATED) {
            String reason = command.notes().isEmpty() ? "" : " " + command.notes().get(0);
            compilation.warning(call.nameRange(), "'" + call.name() + "' is deprecated." + reason);
        }
        if (command.warning() != null) {
            compilation.warning(call.nameRange(), command.warning());
        }
        return spec;
    }

    /** Compiles a call to a new item. Returns empty after an error was reported. */
    public Optional<ObjectNode> compileCall(CommandCategory category, Expr.Call call) {
        return lookup(category, call).flatMap(spec -> {
            ObjectNode item = Items.create(spec.classType());
            return writeArguments(spec, call, item) ? Optional.of(item) : Optional.empty();
        });
    }

    /**
     * Checks the arguments of a call against a command and writes them into an item.
     *
     * @return false when there was an error
     */
    public boolean writeArguments(CommandSpec spec, Expr.Call call, ObjectNode item) {
        return writeArguments(spec, call, item, true);
    }

    /**
     * Writes the arguments of a call into an item.
     *
     * @param checkRequired false when the call gives only some of the command's fields, as
     *                      {@code prompt(...)} does for SetVariableAction
     * @return false when there was an error
     */
    public boolean writeArguments(CommandSpec spec, Expr.Call call, ObjectNode item, boolean checkRequired) {
        int errorsBefore = compilation.diagnostics().errorCount();
        Map<ParamSpec, Expr.Argument> bound = bind(spec, call);

        // Compile the given arguments first, so required-when conditions can see their values.
        for (ParamSpec param : spec.params()) {
            Expr.Argument argument = bound.get(param);
            if (argument != null) {
                compileArgument(spec, param, argument, item);
            }
        }
        for (ParamSpec param : spec.params()) {
            if (checkRequired && !bound.containsKey(param)) {
                writeMissing(spec, param, call, item);
            }
        }
        for (Map.Entry<ParamSpec, Expr.Argument> entry : bound.entrySet()) {
            warnIfUnused(spec, entry.getKey(), entry.getValue(), item);
        }
        return compilation.diagnostics().errorCount() == errorsBefore;
    }

    /** Matches each argument of the call to a parameter. */
    private Map<ParamSpec, Expr.Argument> bind(CommandSpec spec, Expr.Call call) {
        Map<ParamSpec, Expr.Argument> bound = new LinkedHashMap<>();
        boolean unnamedSeen = false;
        for (Expr.Argument argument : call.arguments()) {
            Optional<ParamSpec> param;
            if (argument.name() == null) {
                if (unnamedSeen) {
                    compilation.error(argument.range(), "Only one argument can be written without a name. Write the "
                            + "others as name: value. " + spec.name() + " takes: " + String.join(", ", spec.argumentNames()) + ".");
                    continue;
                }
                unnamedSeen = true;
                param = spec.positionalParam();
                if (param.isEmpty()) {
                    compilation.error(argument.range(), spec.name() + " has no argument that can be written without a name. "
                            + "Write name: value. " + spec.name() + " takes: " + describeArguments(spec) + ".");
                    continue;
                }
            } else {
                param = spec.param(argument.name());
                if (param.isEmpty()) {
                    compilation.error(argument.range(), spec.name() + " has no argument '" + argument.name() + "'."
                            + Suggestions.didYouMean(argument.name(), spec.argumentNames())
                            + " It takes: " + describeArguments(spec) + ".");
                    continue;
                }
            }
            if (bound.containsKey(param.get())) {
                compilation.error(argument.range(), "The argument '" + param.get().name() + "' is given twice.");
                continue;
            }
            bound.put(param.get(), argument);
        }
        return bound;
    }

    private void compileArgument(CommandSpec spec, ParamSpec param, Expr.Argument argument, ObjectNode item) {
        if (argument.value() instanceof Expr.NullLiteral) {
            if (param.hasOutputField()) {
                JsonPaths.put(item, param.outputPath(), item.nullNode());
            }
            return;
        }
        ArgumentContext context = new ArgumentContext(compilation, spec, param, argument.value(), item);
        compilation.kinds().handler(param.kind()).compile(context);
        omitIfDefault(param, item);
    }

    /** Leaves out an optional argument that holds the value MacroDroid uses anyway. */
    private void omitIfDefault(ParamSpec param, ObjectNode item) {
        if (param.required() || !param.hasOutputField()) {
            return;
        }
        if (param.isDefault(JsonPaths.get(item, param.outputPath()))) {
            JsonPaths.remove(item, param.outputPath());
        }
    }

    private void writeMissing(CommandSpec spec, ParamSpec param, Expr.Call call, ObjectNode item) {
        if (!param.hasOutputField()) {
            return;
        }
        if (param.kind() == ParamKind.FIXED) {
            JsonPaths.put(item, param.outputPath(), param.defaultJson());
            return;
        }
        if (param.isFilledByCompiler() || JsonPaths.get(item, param.outputPath()) != null) {
            return;
        }
        boolean required = param.required()
                || ParamCondition.evaluate(param.requiredWhen(), spec, p -> currentValue(p, item)).orElse(false);
        if (!required) {
            return;
        }
        if (param.required() && param.defaultValue() != null) {
            JsonPaths.put(item, param.outputPath(), param.defaultJson());
            return;
        }
        if (param.required()) {
            compilation.error(call.nameRange(), spec.name() + " needs the argument '" + param.name() + "'."
                    + describeParam(param));
        } else {
            // The mapping's conditions do not know every alternative a command has (playSound
            // can use all-files access instead of a dynamic file name), so this is a warning.
            compilation.warning(call.nameRange(), spec.name() + " probably needs the argument '" + param.name()
                    + "' when " + param.requiredWhen() + "." + describeParam(param));
        }
    }

    private void warnIfUnused(CommandSpec spec, ParamSpec param, Expr.Argument argument, ObjectNode item) {
        Optional<Boolean> used = ParamCondition.evaluate(param.usedWhen(), spec, p -> currentValue(p, item));
        if (used.isPresent() && !used.get()) {
            compilation.warning(argument.range(), "'" + param.name() + "' is used only when " + param.usedWhen()
                    + ". MacroDroid ignores it here.");
        }
    }

    /** The JSON value a parameter has in the item, or its default when it is not written. */
    private static JsonNode currentValue(ParamSpec param, ObjectNode item) {
        JsonNode written = param.hasOutputField() ? JsonPaths.get(item, param.outputPath()) : null;
        if (written != null) {
            return written;
        }
        return param.defaultValue() == null ? null : param.defaultJson();
    }

    private String unknownCommandMessage(CommandCategory category, String name) {
        for (CommandCategory other : CommandCategory.values()) {
            if (other != category && compilation.mapping().command(other, name).isPresent()) {
                return "'" + name + "' is " + article(other) + " " + other.displayName() + ", not "
                        + article(category) + " " + category.displayName() + ". " + hintFor(category, other);
            }
        }
        List<String> names = new ArrayList<>(compilation.mapping().commandNames(category));
        return "Unknown " + category.displayName() + " '" + name + "'." + Suggestions.didYouMean(name, names);
    }

    private static String hintFor(CommandCategory wanted, CommandCategory found) {
        if (wanted == CommandCategory.CONSTRAINT && found == CommandCategory.TRIGGER) {
            return "A condition needs a constraint. Look for the constraint that checks the same state.";
        }
        return "Put it in the " + found.sectionName() + " block.";
    }

    private static String article(CommandCategory category) {
        return category == CommandCategory.ACTION ? "an" : "a";
    }

    private static String describeArguments(CommandSpec spec) {
        List<String> names = spec.argumentNames();
        return names.isEmpty() ? "no arguments" : String.join(", ", names);
    }

    /** A hint about what to write for a missing argument. */
    private static String describeParam(ParamSpec param) {
        if (param.constants().isEmpty() || param.constants().size() > 8) {
            return "";
        }
        return " Use one of: " + String.join(", ", param.constants().stream().map(ConstantSpec::name).toList()) + ".";
    }
}
