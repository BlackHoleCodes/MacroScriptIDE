package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.TriggerStatement;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Compiles the triggers block, and trigger calls nested in actions such as waitFor. */
public final class TriggerCompiler {

    private final Compilation compilation;

    TriggerCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    /**
     * Gives each labeled trigger a placeholder ID. This runs before anything else is compiled,
     * so {@code triggeredBy(label)} works wherever the triggers block is in the file.
     */
    void registerLabels(List<TriggerStatement> statements) {
        for (TriggerStatement statement : statements) {
            if (statement.label() == null) {
                continue;
            }
            TriggerLabel label = new TriggerLabel(statement.label(), compilation.guids().next());
            if (!compilation.scope().addLabel(label)) {
                compilation.error(statement.labelRange(), "The label '" + statement.label() + "' is used twice.");
            }
        }
    }

    List<ObjectNode> compileBlock(List<TriggerStatement> statements) {
        List<ObjectNode> items = new ArrayList<>();
        for (TriggerStatement statement : statements) {
            compile(statement).ifPresent(items::add);
        }
        return items;
    }

    private Optional<ObjectNode> compile(TriggerStatement statement) {
        Optional<ObjectNode> compiled = compileCall(statement.call());
        if (compiled.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = compiled.get();
        if (statement.label() != null) {
            compilation.scope().label(statement.label()).ifPresent(label -> item.put("m_SIGUID", label.guid()));
        }
        if (statement.when() != null) {
            compilation.conditions().compile(statement.when()).ifPresent(list -> Items.setConstraints(item, list));
        }
        Items.applyMeta(item, statement.meta());
        return Optional.of(item);
    }

    /** Compiles one trigger call, for example {@code shakeDevice()} or {@code raw(...)}. */
    public Optional<ObjectNode> compileCall(Expr.Call call) {
        if (call.name().equals("raw")) {
            return compilation.helpers().raw(call);
        }
        return compilation.commands().compileCall(CommandCategory.TRIGGER, call);
    }
}
