package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import com.blackholecodes.macroscript.kinds.ParamKinds;
import com.blackholecodes.macroscript.mapping.Mapping;

/**
 * The state of the compilation of one file, and the parts of the compiler that work on it.
 *
 * <p>The parts get the compilation in their constructor and ask it for each other when
 * they need one. This keeps the parts small and lets each one be read on its own.
 */
public final class Compilation {

    private final Mapping mapping;
    private final Diagnostics diagnostics;
    private final ActionBlockLinker actionBlocks;
    private final GuidAllocator guids;
    private final Scope scope = new Scope();

    private final ReferenceResolver references = new ReferenceResolver(this);
    private final MagicText magicText = new MagicText(this);
    private final CommandEmitter commands = new CommandEmitter(this);
    private final ConditionCompiler conditions = new ConditionCompiler(this);
    private final TriggerCompiler triggers = new TriggerCompiler(this);
    private final ActionCompiler actions = new ActionCompiler(this);
    private final AssignmentCompiler assignments = new AssignmentCompiler(this);
    private final HelperCalls helpers = new HelperCalls(this);

    Compilation(Mapping mapping, Diagnostics diagnostics, ActionBlockLinker actionBlocks, GuidAllocator guids) {
        this.mapping = mapping;
        this.diagnostics = diagnostics;
        this.actionBlocks = actionBlocks;
        this.guids = guids;
    }

    public Mapping mapping() {
        return mapping;
    }

    public Scope scope() {
        return scope;
    }

    public GuidAllocator guids() {
        return guids;
    }

    public ActionBlockLinker actionBlocks() {
        return actionBlocks;
    }

    public ParamKinds kinds() {
        return ParamKinds.instance();
    }

    public ReferenceResolver references() {
        return references;
    }

    public MagicText magicText() {
        return magicText;
    }

    public CommandEmitter commands() {
        return commands;
    }

    public ConditionCompiler conditions() {
        return conditions;
    }

    public TriggerCompiler triggers() {
        return triggers;
    }

    public ActionCompiler actions() {
        return actions;
    }

    public AssignmentCompiler assignments() {
        return assignments;
    }

    public HelperCalls helpers() {
        return helpers;
    }

    public void error(SourceRange range, String message) {
        diagnostics.error(range, message);
    }

    public void warning(SourceRange range, String message) {
        diagnostics.warning(range, message);
    }

    Diagnostics diagnostics() {
        return diagnostics;
    }
}
