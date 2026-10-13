package com.blackholecodes.macroscript.compiler;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The names that code in one file can refer to: variables, trigger labels and loop aliases. */
public final class Scope {

    private final Map<String, VariableSymbol> variables = new LinkedHashMap<>();
    private final Map<String, TriggerLabel> labels = new LinkedHashMap<>();
    private final Deque<LoopScope> loops = new ArrayDeque<>();

    /** Adds a variable. Returns false when the name is already declared. */
    boolean declare(VariableSymbol symbol) {
        return variables.putIfAbsent(symbol.name(), symbol) == null;
    }

    public Optional<VariableSymbol> variable(String name) {
        return Optional.ofNullable(variables.get(name));
    }

    public Collection<VariableSymbol> variables() {
        return variables.values();
    }

    /** Adds a trigger label. Returns false when the label is already used. */
    boolean addLabel(TriggerLabel label) {
        return labels.putIfAbsent(label.name(), label) == null;
    }

    public Optional<TriggerLabel> label(String name) {
        return Optional.ofNullable(labels.get(name));
    }

    public Collection<String> labelNames() {
        return labels.keySet();
    }

    void enterLoop(LoopScope loop) {
        loops.push(loop);
    }

    void exitLoop() {
        loops.pop();
    }

    /** The for loop that the current statement is directly inside, if any. */
    public Optional<LoopScope> innermostLoop() {
        return Optional.ofNullable(loops.peek());
    }

    /** True when an outer for loop (not the innermost one) uses the name as an alias. */
    public boolean isOuterLoopAlias(String name) {
        boolean first = true;
        for (LoopScope loop : loops) {
            if (!first && (loop.keyAlias().equals(name) || loop.valueAlias().equals(name))) {
                return true;
            }
            first = false;
        }
        return false;
    }
}
