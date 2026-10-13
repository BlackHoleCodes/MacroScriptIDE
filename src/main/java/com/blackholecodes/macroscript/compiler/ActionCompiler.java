package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Declaration;
import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.Stmt;
import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.DurationUnit;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Compiles the statements of the actions block to MacroDroid's flat action list. Braces
 * become open and close marker actions: IfConditionAction ... EndIfAction and
 * LoopAction ... EndLoopAction.
 */
public final class ActionCompiler {

    // LoopAction m_option values.
    private static final int LOOP_FIXED_COUNT = 0;
    private static final int LOOP_WHILE_DO = 1;
    private static final int LOOP_DO_WHILE = 2;
    private static final int LOOP_ENDLESS = 3;
    private static final int LOOP_TIMED = 4;

    /** The units of a timed loop, largest first, with their timedTimeUnit codes. */
    private static final List<DurationUnit> TIMED_UNITS = List.of(DurationUnit.HOURS, DurationUnit.MINUTES, DurationUnit.SECONDS);
    private static final int[] TIMED_UNIT_CODES = {2, 1, 0};

    private final Compilation compilation;
    private int loopDepth;

    ActionCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    public List<ObjectNode> compileBlock(List<Stmt> statements) {
        List<ObjectNode> actions = new ArrayList<>();
        compileStatements(statements, actions);
        return actions;
    }

    private void compileStatements(List<Stmt> statements, List<ObjectNode> out) {
        for (Stmt statement : statements) {
            compileStatement(statement, out);
        }
    }

    private void compileStatement(Stmt statement, List<ObjectNode> out) {
        switch (statement) {
            case Stmt.CallStatement s -> compileCall(s).ifPresent(out::add);
            case Stmt.Assignment s -> compilation.assignments().compile(s).ifPresent(out::add);
            case Stmt.If s -> compileIf(s, out);
            case Stmt.While s -> compileWhile(s, out);
            case Stmt.DoWhile s -> compileDoWhile(s, out);
            case Stmt.Repeat s -> compileRepeat(s, out);
            case Stmt.For s -> compileFor(s, out);
            case Stmt.Break s -> compileLoopControl(s, "breakFromLoop", "break", out);
            case Stmt.Continue s -> compileLoopControl(s, "continueLoop", "continue", out);
            case Stmt.Return s -> out.add(marker("terminateRunning", s));
        }
    }

    // ----- calls -------------------------------------------------------------------------

    private Optional<ObjectNode> compileCall(Stmt.CallStatement statement) {
        Expr.Call call = statement.call();
        Optional<ObjectNode> compiled = switch (call.name()) {
            case "wait" -> compilation.helpers().wait(call);
            case "waitFor" -> compilation.helpers().waitFor(call);
            case "callBlock" -> compilation.actionBlocks().callBlock(compilation, call);
            case "raw" -> compilation.helpers().raw(call);
            case "confirm" -> misplaced(call, "confirm() is a condition. Write if (confirm(title: \"...\", text: \"...\")) { }.");
            case "random", "prompt" -> misplaced(call, call.name() + "() gives a value. Write it on the right side of an "
                    + "assignment: x = " + call.name() + "(...);");
            case "triggeredBy", "xor" -> misplaced(call, call.name() + "() is a condition. Use it in if (...) or when (...).");
            default -> compilation.commands().compileCall(CommandCategory.ACTION, call);
        };
        compiled.ifPresent(item -> {
            if (statement.when() != null) {
                compilation.conditions().compile(statement.when()).ifPresent(list -> Items.setConstraints(item, list));
            }
            Items.applyMeta(item, statement.meta());
        });
        return compiled;
    }

    private Optional<ObjectNode> misplaced(Expr.Call call, String message) {
        compilation.error(call.nameRange(), message);
        return Optional.empty();
    }

    // ----- if ----------------------------------------------------------------------------

    private void compileIf(Stmt.If statement, List<ObjectNode> out) {
        for (int i = 0; i < statement.branches().size(); i++) {
            Stmt.Branch branch = statement.branches().get(i);
            boolean first = i == 0;
            Optional<ObjectNode> opener = branch.condition() instanceof Expr.Call call && call.name().equals("confirm")
                    ? compilation.helpers().confirm(call, first ? "ifConfirmedThen" : "elseIfConfirmedThen")
                    : conditionalItem(first ? "ifCondition" : "elseIfCondition", branch.condition());
            opener.ifPresent(item -> {
                if (first) {
                    Items.applyMeta(item, statement.meta());
                }
                out.add(item);
            });
            compileStatements(branch.body(), out);
        }
        if (statement.elseBody() != null) {
            out.add(marker("elseOf", null));
            compileStatements(statement.elseBody(), out);
        }
        out.add(marker("endIf", null));
    }

    /** An IfConditionAction or ElseIfConditionAction with the condition in m_constraintList. */
    private Optional<ObjectNode> conditionalItem(String commandName, Expr condition) {
        ObjectNode item = Items.create(classType(commandName));
        Optional<ConstraintList> constraints = compilation.conditions().compile(condition);
        constraints.ifPresent(list -> Items.setConstraints(item, list));
        // Keep the item even after an error, so the list stays balanced for later checks.
        return Optional.of(item);
    }

    // ----- loops -------------------------------------------------------------------------

    private void compileWhile(Stmt.While statement, List<ObjectNode> out) {
        ObjectNode loop = loopItem(statement);
        if (statement.condition() instanceof Expr.BoolLiteral bool) {
            if (!bool.value()) {
                compilation.error(bool.range(), "while (false) never runs. Remove the loop.");
            }
            loop.put("m_option", LOOP_ENDLESS);
        } else {
            loop.put("m_option", LOOP_WHILE_DO);
            compilation.conditions().compile(statement.condition()).ifPresent(list -> Items.setConstraints(loop, list));
        }
        compileLoopBody(loop, statement.body(), out);
    }

    private void compileDoWhile(Stmt.DoWhile statement, List<ObjectNode> out) {
        ObjectNode loop = loopItem(statement);
        if (statement.condition() instanceof Expr.BoolLiteral bool && bool.value()) {
            loop.put("m_option", LOOP_ENDLESS);
        } else {
            loop.put("m_option", LOOP_DO_WHILE);
            compilation.conditions().compile(statement.condition()).ifPresent(list -> Items.setConstraints(loop, list));
        }
        compileLoopBody(loop, statement.body(), out);
    }

    private void compileRepeat(Stmt.Repeat statement, List<ObjectNode> out) {
        ObjectNode loop = loopItem(statement);
        switch (statement.count()) {
            case Expr.IntLiteral count -> {
                if (count.value() < 1) {
                    compilation.error(count.range(), "repeat needs a count of 1 or more.");
                }
                loop.put("m_option", LOOP_FIXED_COUNT);
                loop.put("m_fixedOptionCount", count.value());
            }
            case Expr.DurationLiteral duration -> {
                loop.put("m_option", LOOP_TIMED);
                writeTimedDuration(loop, duration);
            }
            default -> {
                loop.put("m_option", LOOP_FIXED_COUNT);
                writeCountVariable(loop, statement.count());
            }
        }
        compileLoopBody(loop, statement.body(), out);
    }

    /** Writes the duration of a timed loop in the largest unit that gives a whole number. */
    private void writeTimedDuration(ObjectNode loop, Expr.DurationLiteral duration) {
        for (int i = 0; i < TIMED_UNITS.size(); i++) {
            long unitMillis = TIMED_UNITS.get(i).millis();
            if (duration.millis() >= unitMillis && duration.millis() % unitMillis == 0) {
                loop.put("timedDurationValue", duration.millis() / unitMillis);
                loop.put("timedTimeUnit", TIMED_UNIT_CODES[i]);
                return;
            }
        }
        compilation.error(duration.range(), "A timed repeat must be a whole number of seconds.");
    }

    private void writeCountVariable(ObjectNode loop, Expr countExpr) {
        Optional<Reference.Variable> variable = compilation.references().resolveVariable(countExpr);
        if (variable.isEmpty()) {
            return;
        }
        VarType type = variable.get().knownType();
        if (type != null && type != VarType.INT) {
            compilation.error(countExpr.range(), "repeat needs an int, a duration or an int variable.");
            return;
        }
        loop.set("m_fixedOptionVariable", variable.get().symbol().toReference());
        if (variable.get().isEntry()) {
            DictionaryKeys.render(compilation, variable.get().keys())
                    .ifPresent(keys -> loop.set("fixedOptionDictionaryKeys", DictionaryKeys.keysObject(keys)));
        }
    }

    private void compileFor(Stmt.For statement, List<ObjectNode> out) {
        ObjectNode iterate = Items.create(classType("iterateDictionary"));
        Items.applyMeta(iterate, statement.meta());
        boolean isArray = false;
        Optional<Reference.Variable> source = compilation.references().resolveVariable(statement.source());
        if (source.isPresent()) {
            VariableSymbol symbol = source.get().symbol();
            if (!symbol.type().isCollection()) {
                compilation.error(statement.source().range(), "for (key, value : ...) needs a dict or array, but '"
                        + symbol.name() + "' is a " + symbol.type().keyword() + ".");
            } else {
                isArray = isArray(symbol, source.get().keys());
                iterate.put("variableName", symbol.name());
                iterate.put("isArray", isArray);
                List<String> keys = DictionaryKeys.render(compilation, source.get().keys()).orElse(List.of());
                iterate.set("dictionaryKeys", DictionaryKeys.keysObject(keys));
            }
        }
        checkAlias(statement.keyAlias(), statement);
        checkAlias(statement.valueAlias(), statement);
        out.add(iterate);

        compilation.scope().enterLoop(new LoopScope(statement.keyAlias(), statement.valueAlias(), isArray));
        loopDepth++;
        compileStatements(statement.body(), out);
        loopDepth--;
        compilation.scope().exitLoop();
        out.add(marker("endLoop", null));
    }

    private void checkAlias(String alias, Stmt.For statement) {
        if (compilation.scope().variable(alias).isPresent()) {
            compilation.error(statement.range(), "The loop name '" + alias + "' is already a variable. Use another name.");
        }
    }

    /**
     * True when the loop walks an array. For a nested entry, the initial value of the
     * variable tells whether the entry is an array; when it has none, a dict is assumed.
     */
    private static boolean isArray(VariableSymbol symbol, List<Expr> keys) {
        if (keys.isEmpty()) {
            return symbol.type() == VarType.ARRAY;
        }
        Expr value = initialValueAt(symbol.declaration(), keys);
        return value instanceof Expr.ListLiteral;
    }

    private static Expr initialValueAt(Declaration declaration, List<Expr> keys) {
        Expr current = declaration.initializer();
        for (Expr key : keys) {
            current = switch (current) {
                case Expr.ObjectLiteral object when key instanceof Expr.StringLiteral s && s.plainText().isPresent() ->
                        object.entries().stream().filter(e -> e.key().equals(s.plainText().get()))
                                .map(Expr.ObjectEntry::value).findFirst().orElse(null);
                case Expr.ListLiteral list when key instanceof Expr.IntLiteral i
                        && i.value() >= 0 && i.value() < list.elements().size() -> list.elements().get((int) i.value());
                case null, default -> null;
            };
        }
        return current;
    }

    private ObjectNode loopItem(Stmt statement) {
        ObjectNode loop = Items.create(classType("loop"));
        Items.applyMeta(loop, statement.meta());
        return loop;
    }

    private void compileLoopBody(ObjectNode loop, List<Stmt> body, List<ObjectNode> out) {
        out.add(loop);
        loopDepth++;
        compileStatements(body, out);
        loopDepth--;
        out.add(marker("endLoop", null));
    }

    private void compileLoopControl(Stmt statement, String commandName, String keyword, List<ObjectNode> out) {
        if (loopDepth == 0) {
            compilation.error(statement.range(), keyword + " can only be used inside a loop.");
        }
        out.add(marker(commandName, statement));
    }

    // ----- helpers -----------------------------------------------------------------------

    /** An item with no fields, such as EndIfAction. The statement gives its annotations, if any. */
    private ObjectNode marker(String commandName, Stmt statement) {
        ObjectNode item = Items.create(classType(commandName));
        if (statement != null) {
            Items.applyMeta(item, statement.meta());
        }
        return item;
    }

    /** The class type of a syntax command, taken from the mapping. */
    private String classType(String commandName) {
        return compilation.mapping().command(CommandCategory.ACTION, commandName).orElseThrow().classType();
    }
}
