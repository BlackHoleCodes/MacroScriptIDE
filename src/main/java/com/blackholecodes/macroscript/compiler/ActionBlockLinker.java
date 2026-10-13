package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.parser.ParseResult;
import com.blackholecodes.macroscript.parser.SourceParser;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handles calls of action blocks: {@code callBlock("Name", ...)} and {@code block("Name", ...)}.
 *
 * <p>The first call of a block finds its {@code .ms} file, compiles it and gives it a
 * placeholder ID. Every block that the macro calls, directly or through other blocks, goes
 * into {@code macro.exportedActionBlocks} once.
 */
public final class ActionBlockLinker {

    private final ActionBlockResolver resolver;
    private final Mapping mapping;
    private final GuidAllocator guids;
    private final Map<String, LinkedBlock> linked = new LinkedHashMap<>();
    private final Set<String> inProgress = new HashSet<>();
    private final List<Diagnostic> blockDiagnostics = new ArrayList<>();

    /** A compiled action block. {@code macro} is null when the block has errors. */
    private record LinkedBlock(String name, long guid, ObjectNode macro, Map<String, VariableSymbol> inputs,
                               Map<String, VariableSymbol> outputs) {
    }

    ActionBlockLinker(ActionBlockResolver resolver, Mapping mapping, GuidAllocator guids) {
        this.resolver = resolver;
        this.mapping = mapping;
        this.guids = guids;
    }

    /** {@code callBlock("Name", inputs: {...}, outputs: {...}, async: true)} as an ActionBlockAction. */
    public Optional<ObjectNode> callBlock(Compilation caller, Expr.Call call) {
        String classType = mapping.command(CommandCategory.ACTION, "actionBlock").orElseThrow().classType();
        ObjectNode item = Items.create(classType);
        return writeBlockReference(caller, call, item, true) ? Optional.of(item) : Optional.empty();
    }

    /** {@code block("Name", inputs: {...}, outputs: {...})} as an ActionBlockData object. */
    public Optional<ObjectNode> blockData(Compilation caller, Expr.Call call) {
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        return writeBlockReference(caller, call, data, false) ? Optional.of(data) : Optional.empty();
    }

    /** The compiled blocks, for {@code macro.exportedActionBlocks}. */
    List<ObjectNode> exportedBlocks() {
        return linked.values().stream().map(LinkedBlock::macro).filter(java.util.Objects::nonNull).toList();
    }

    /** The errors and warnings in the files of the called blocks. */
    List<Diagnostic> blockDiagnostics() {
        return List.copyOf(blockDiagnostics);
    }

    // ----- arguments ---------------------------------------------------------------------

    private boolean writeBlockReference(Compilation caller, Expr.Call call, ObjectNode target, boolean allowAsync) {
        List<Expr.Argument> args = call.arguments();
        if (args.isEmpty() || args.get(0).name() != null
                || !(args.get(0).value() instanceof Expr.StringLiteral nameLiteral) || nameLiteral.hasInterpolation()) {
            caller.error(call.range(), call.name() + " needs the block name first: "
                    + call.name() + "(\"Block Name\", inputs: {...}, outputs: {...}).");
            return false;
        }
        String name = nameLiteral.plainText().orElseThrow();
        Optional<LinkedBlock> block = link(caller, name, nameLiteral.range());
        if (block.isEmpty()) {
            return false;
        }
        ObjectNode inputVars = JsonNodeFactory.instance.objectNode();
        ObjectNode inputDictionaries = JsonNodeFactory.instance.objectNode();
        ObjectNode outputVars = JsonNodeFactory.instance.objectNode();
        ObjectNode outputDictionaries = JsonNodeFactory.instance.objectNode();
        boolean async = false;
        boolean ok = true;
        for (Expr.Argument argument : args.subList(1, args.size())) {
            String argumentName = argument.name() == null ? "" : argument.name();
            switch (argumentName) {
                case "inputs" -> ok &= readInputs(caller, block.get(), argument.value(), inputVars, inputDictionaries);
                case "outputs" -> ok &= readOutputs(caller, block.get(), argument.value(), outputVars, outputDictionaries);
                case "async" -> {
                    if (allowAsync && argument.value() instanceof Expr.BoolLiteral bool) {
                        async = bool.value();
                    } else {
                        caller.error(argument.range(), allowAsync ? "async needs true or false."
                                : "async can only be used with callBlock.");
                        ok = false;
                    }
                }
                default -> {
                    caller.error(argument.range(), call.name() + " takes the block name, inputs: {...}, outputs: {...}"
                            + (allowAsync ? " and async: true." : "."));
                    ok = false;
                }
            }
        }
        if (!ok) {
            return false;
        }
        target.put("actionBlockId", block.get().guid());
        target.put("actionBlockName", name);
        target.set("inputVarsMap", inputVars);
        target.set("outputVarsMap", outputVars);
        if (async) {
            target.put("continueActionsWithoutWaiting", true);
        }
        if (!inputDictionaries.isEmpty()) {
            target.set("inputDictionaryMap", inputDictionaries);
        }
        if (!outputDictionaries.isEmpty()) {
            target.set("outputDictionaryMap", outputDictionaries);
        }
        return true;
    }

    /** inputs: {blockVariable: value}. A scalar input gets magic text; a dict or array input gets a variable name. */
    private boolean readInputs(Compilation caller, LinkedBlock block, Expr value, ObjectNode vars, ObjectNode dictionaries) {
        if (!(value instanceof Expr.ObjectLiteral object)) {
            caller.error(value.range(), "inputs needs an object: inputs: {blockVariable: value}.");
            return false;
        }
        boolean ok = true;
        for (Expr.ObjectEntry entry : object.entries()) {
            VariableSymbol input = block.inputs().get(entry.key());
            if (input == null) {
                caller.error(entry.keyRange(), "The action block '" + block.name() + "' has no input '" + entry.key()
                        + "'." + listNames(block.inputs()));
                ok = false;
            } else if (input.type().isCollection()) {
                ok &= writeVariable(caller, entry.value(), entry.key(), vars, dictionaries);
            } else {
                Optional<String> text = caller.magicText().textValue(entry.value());
                text.ifPresent(t -> vars.put(entry.key(), t));
                ok &= text.isPresent();
            }
        }
        return ok;
    }

    /** outputs: {blockVariable: macroVariable}. */
    private boolean readOutputs(Compilation caller, LinkedBlock block, Expr value, ObjectNode vars, ObjectNode dictionaries) {
        if (!(value instanceof Expr.ObjectLiteral object)) {
            caller.error(value.range(), "outputs needs an object: outputs: {blockVariable: myVariable}.");
            return false;
        }
        boolean ok = true;
        for (Expr.ObjectEntry entry : object.entries()) {
            VariableSymbol output = block.outputs().get(entry.key());
            if (output == null) {
                caller.error(entry.keyRange(), "The action block '" + block.name() + "' has no output '" + entry.key()
                        + "'." + listNames(block.outputs()));
                ok = false;
                continue;
            }
            Optional<Reference.Variable> target = caller.references().resolveVariable(entry.value());
            if (target.isPresent() && !target.get().isEntry() && target.get().symbol().type() != output.type()) {
                caller.error(entry.value().range(), "The output '" + entry.key() + "' is a " + output.type().keyword()
                        + ", but '" + target.get().symbol().name() + "' is a " + target.get().symbol().type().keyword() + ".");
                ok = false;
                continue;
            }
            ok &= target.isPresent() && writeVariable(caller, entry.value(), entry.key(), vars, dictionaries);
        }
        return ok;
    }

    private static boolean writeVariable(Compilation caller, Expr value, String key, ObjectNode vars, ObjectNode dictionaries) {
        Optional<Reference.Variable> variable = caller.references().resolveVariable(value);
        if (variable.isEmpty()) {
            return false;
        }
        vars.put(key, variable.get().symbol().name());
        if (variable.get().isEntry()) {
            Optional<List<String>> keys = DictionaryKeys.render(caller, variable.get().keys());
            if (keys.isEmpty()) {
                return false;
            }
            dictionaries.set(key, DictionaryKeys.keysObject(keys.get()));
        }
        return true;
    }

    private static String listNames(Map<String, VariableSymbol> variables) {
        return variables.isEmpty() ? " It has none." : " It has: " + String.join(", ", variables.keySet()) + ".";
    }

    // ----- linking -----------------------------------------------------------------------

    private Optional<LinkedBlock> link(Compilation caller, String name, SourceRange at) {
        LinkedBlock existing = linked.get(name);
        if (existing != null) {
            return checkCompiled(caller, existing, at);
        }
        if (inProgress.contains(name)) {
            caller.error(at, "The action block '" + name + "' calls itself, directly or through other blocks. "
                    + "MacroDroid cannot import that.");
            return Optional.empty();
        }
        Optional<SourceUnit> source = resolver.find(name);
        if (source.isEmpty()) {
            caller.error(at, "No action block is named '" + name + "'. Create a .ms file in the project with "
                    + "@actionBlock and @name(\"" + name + "\").");
            return Optional.empty();
        }
        inProgress.add(name);
        LinkedBlock block = compileBlock(name, source.get());
        inProgress.remove(name);
        linked.put(name, block);
        return checkCompiled(caller, block, at);
    }

    private static Optional<LinkedBlock> checkCompiled(Compilation caller, LinkedBlock block, SourceRange at) {
        if (block.macro() == null) {
            caller.error(at, "The action block '" + block.name() + "' has errors. Fix them in its file first.");
            return Optional.empty();
        }
        return Optional.of(block);
    }

    private LinkedBlock compileBlock(String name, SourceUnit source) {
        Diagnostics diagnostics = new Diagnostics();
        ParseResult parsed = SourceParser.parse(source.text());
        diagnostics.addAll(parsed.diagnostics());
        Compilation compilation = new Compilation(mapping, diagnostics, this, guids);
        long guid = guids.next();
        ObjectNode macro = new MacroCompiler(compilation).compile(parsed.file(), source.baseName());
        macro.put("m_GUID", guid);
        diagnostics.all().forEach(d -> blockDiagnostics.add(d.inFile(source.path())));

        Map<String, VariableSymbol> inputs = new LinkedHashMap<>();
        Map<String, VariableSymbol> outputs = new LinkedHashMap<>();
        for (VariableSymbol variable : compilation.scope().variables()) {
            if (variable.isInput()) {
                inputs.put(variable.name(), variable);
            }
            if (variable.isOutput()) {
                outputs.put(variable.name(), variable);
            }
        }
        return new LinkedBlock(name, guid, diagnostics.hasErrors() ? null : macro, inputs, outputs);
    }
}
