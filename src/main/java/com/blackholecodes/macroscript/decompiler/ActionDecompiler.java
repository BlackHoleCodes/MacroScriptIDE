package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Writes MacroDroid's flat action list as structured MacroScript statements. The open and
 * close marker actions (IfConditionAction ... EndIfAction, LoopAction ... EndLoopAction)
 * become braces again.
 *
 * <p>When the markers do not pair up, or a marker has a form MacroScript cannot write, every
 * action is written as {@code raw(...)} in order. The compiler then gives back the same list.
 */
final class ActionDecompiler {

    /** The fields of a PauseAction that {@code wait(...)} can write. */
    private static final Set<String> WAIT_FIELDS = Set.of("m_delayInSeconds", "m_delayInMilliSeconds", "m_variable",
            "unitForVariables", "varDictionaryKeys");
    /** The fields of a WaitUntilTriggerAction that {@code waitFor(...)} can write. */
    private static final Set<String> WAIT_FOR_FIELDS = Set.of("triggersToWaitFor", "timeoutSeconds", "timeoutEnabled",
            "continueOnTimeout", "booleanVariableName", "booleanVarDictionaryKeys");
    private static final String[] TIMED_UNITS = {"s", "m", "h"};

    /** What an open marker action is waiting for. */
    private enum Open { IF, LOOP, FOR }

    private final Decompilation decompilation;
    private final AssignmentDecompiler assignments;
    private final Deque<JsonNode> openLoops = new ArrayDeque<>();

    ActionDecompiler(Decompilation decompilation) {
        this.decompilation = decompilation;
        this.assignments = new AssignmentDecompiler(decompilation);
    }

    void write(JsonNode actions, SourceWriter out) {
        if (!isWellFormed(actions)) {
            decompilation.warn("The action list has open and close actions that do not pair up. "
                    + "The actions are written as raw(...) calls.");
            actions.forEach(item -> writeRaw(item, out));
            return;
        }
        for (JsonNode item : actions) {
            writeAction(item, out);
        }
    }

    // ----- structure check ---------------------------------------------------------------

    /** True when the markers pair up and each one can be written with MacroScript syntax. */
    private boolean isWellFormed(JsonNode actions) {
        Deque<Open> stack = new ArrayDeque<>();
        Deque<Boolean> hasElse = new ArrayDeque<>();
        for (JsonNode item : actions) {
            String type = item.path("m_classType").asText();
            switch (type) {
                case "IfConditionAction", "IfConfirmedThenAction" -> {
                    if (type.equals("IfConditionAction") && !hasConstraints(item)) {
                        return false;
                    }
                    stack.push(Open.IF);
                    hasElse.push(false);
                }
                case "ElseIfConditionAction", "ElseIfConfirmedThenAction" -> {
                    if (stack.peek() != Open.IF || hasElse.peek()
                            || (type.equals("ElseIfConditionAction") && !hasConstraints(item))) {
                        return false;
                    }
                }
                case "ElseAction" -> {
                    if (stack.peek() != Open.IF || hasElse.peek()) {
                        return false;
                    }
                    hasElse.pop();
                    hasElse.push(true);
                }
                case "EndIfAction" -> {
                    if (stack.peek() != Open.IF) {
                        return false;
                    }
                    stack.pop();
                    hasElse.pop();
                }
                case "LoopAction" -> {
                    if (!isWritableLoop(item)) {
                        return false;
                    }
                    stack.push(Open.LOOP);
                }
                case "IterateDictionaryAction" -> {
                    if (!item.path("variableName").isTextual() || hasConstraints(item)) {
                        return false;
                    }
                    stack.push(Open.FOR);
                }
                case "EndLoopAction" -> {
                    if (stack.peek() != Open.LOOP && stack.peek() != Open.FOR) {
                        return false;
                    }
                    stack.pop();
                }
                default -> {
                    // Ordinary actions do not change the structure.
                }
            }
        }
        return stack.isEmpty();
    }

    private static boolean isWritableLoop(JsonNode loop) {
        int option = loop.path("m_option").asInt(0);
        boolean conditional = option == 1 || option == 2;
        if (conditional != hasConstraints(loop)) {
            return false;
        }
        return option != 4 || loop.get("timedVariable") == null || loop.get("timedVariable").isNull();
    }

    private static boolean hasConstraints(JsonNode item) {
        JsonNode list = item.get("m_constraintList");
        return list != null && list.isArray() && !list.isEmpty();
    }

    // ----- statements --------------------------------------------------------------------

    private void writeAction(JsonNode item, SourceWriter out) {
        String type = item.path("m_classType").asText();
        switch (type) {
            case "IfConditionAction" -> {
                MetaLines.write(item, out);
                out.open("if (" + condition(item) + ") {");
            }
            case "IfConfirmedThenAction" -> {
                MetaLines.write(item, out);
                out.open("if (" + confirm(item) + ") {");
            }
            case "ElseIfConditionAction" -> out.reopen("} else if (" + condition(item) + ") {");
            case "ElseIfConfirmedThenAction" -> out.reopen("} else if (" + confirm(item) + ") {");
            case "ElseAction" -> out.reopen("} else {");
            case "EndIfAction" -> out.close("}");
            case "LoopAction" -> openLoop(item, out);
            case "IterateDictionaryAction" -> openFor(item, out);
            case "EndLoopAction" -> closeLoop(out);
            case "BreakFromLoopAction" -> simple(item, "break;", out);
            case "ContinueLoopAction" -> simple(item, "continue;", out);
            case "TerminateRunningAction" -> simple(item, "return;", out);
            default -> writeStatement(item, out);
        }
    }

    private void simple(JsonNode item, String statement, SourceWriter out) {
        MetaLines.write(item, out);
        out.line(statement);
    }

    private void writeStatement(JsonNode item, SourceWriter out) {
        String statement;
        try {
            statement = switch (item.path("m_classType").asText()) {
                case "SetVariableAction" -> assignments.assignment(item);
                case "PauseAction" -> waitCall(item);
                case "WaitUntilTriggerAction" -> waitForCall(item);
                case "ActionBlockAction" -> callBlock(item);
                default -> decompilation.items().mappedCall(item, CommandCategory.ACTION, true);
            };
        } catch (UnrepresentableException e) {
            decompilation.warn(item.path("m_classType").asText() + " is written as raw(...): " + e.getMessage());
            statement = decompilation.items().raw(item, true);
        }
        MetaLines.write(item, out);
        out.line(statement + when(item) + ";");
    }

    private void writeRaw(JsonNode item, SourceWriter out) {
        MetaLines.write(item, out);
        out.line(decompilation.items().raw(item, true) + when(item) + ";");
    }

    /** The {@code when (...)} part for an action with constraints, or an empty string. */
    private String when(JsonNode item) {
        return hasConstraints(item) ? " when (" + condition(item) + ")" : "";
    }

    private String condition(JsonNode item) {
        return decompilation.conditions().list(item.path("m_constraintList"), item.path("m_isOrCondition").asBoolean());
    }

    // ----- loops -------------------------------------------------------------------------

    private void openLoop(JsonNode loop, SourceWriter out) {
        MetaLines.write(loop, out);
        openLoops.push(loop);
        switch (loop.path("m_option").asInt(0)) {
            case 0 -> {
                JsonNode variable = loop.get("m_fixedOptionVariable");
                String count = variable != null && variable.isObject()
                        ? decompilation.variables().register(variable) + decompilation.keySuffix(loop.get("fixedOptionDictionaryKeys"))
                        : Long.toString(loop.path("m_fixedOptionCount").asLong(1));
                out.open("repeat (" + count + ") {");
            }
            case 1 -> out.open("while (" + condition(loop) + ") {");
            case 2 -> out.open("do {");
            case 3 -> out.open("while (true) {");
            default -> {
                int unit = loop.path("timedTimeUnit").asInt(0);
                String suffix = unit >= 0 && unit < TIMED_UNITS.length ? TIMED_UNITS[unit] : "s";
                out.open("repeat (" + loop.path("timedDurationValue").asLong(1) + suffix + ") {");
            }
        }
    }

    private void openFor(JsonNode iterate, SourceWriter out) {
        MetaLines.write(iterate, out);
        openLoops.push(iterate);
        String source = decompilation.variables().registerName(iterate.path("variableName").asText())
                + decompilation.keySuffix(iterate.get("dictionaryKeys"));
        int depth = decompilation.loops().depth();
        String key = aliasName("key", depth);
        String value = aliasName("value", depth);
        decompilation.loops().push(new LoopAliases.Loop(key, value, iterate.path("isArray").asBoolean()));
        out.open("for (" + key + ", " + value + " : " + source + ") {");
    }

    /** {@code key}, or {@code key2} in a nested loop, avoiding the names of variables. */
    private String aliasName(String base, int depth) {
        String name = depth == 0 ? base : base + (depth + 1);
        while (decompilation.variables().isDeclared(name)) {
            name = name + "_";
        }
        return name;
    }

    private void closeLoop(SourceWriter out) {
        JsonNode loop = openLoops.pop();
        if (loop.path("m_classType").asText().equals("IterateDictionaryAction")) {
            decompilation.loops().pop();
            out.close("}");
        } else if (loop.path("m_option").asInt(0) == 2) {
            out.close("} while (" + condition(loop) + ");");
        } else {
            out.close("}");
        }
    }

    // ----- helpers with their own syntax -------------------------------------------------

    /** {@code wait(2s)} or {@code wait(count, MINUTES)}. Other pause settings use pause(...). */
    private String waitCall(JsonNode item) {
        boolean hasDelay = item.has("m_delayInSeconds") || item.has("m_variable");
        if (!hasDelay || !onlyHas(item, WAIT_FIELDS)) {
            return decompilation.items().mappedCall(item, CommandCategory.ACTION, true);
        }
        JsonNode variable = item.get("m_variable");
        if (variable != null && variable.isObject()) {
            String name = decompilation.variables().register(variable) + decompilation.keySuffix(item.get("varDictionaryKeys"));
            CommandSpec pause = decompilation.mapping().command(CommandCategory.ACTION, "pause").orElseThrow();
            String unit = pause.param("unitForVariables").flatMap(p -> p.constantWithValue(item.path("unitForVariables")))
                    .map(ConstantSpec::name).orElse("SECONDS");
            return "wait(" + name + (unit.equals("SECONDS") ? "" : ", " + unit) + ")";
        }
        long seconds = item.path("m_delayInSeconds").asLong(1);
        long millis = item.path("m_delayInMilliSeconds").asLong(0);
        String duration = millis == 0 ? seconds + "s" : seconds == 0 ? millis + "ms" : seconds + "s" + millis + "ms";
        return "wait(" + duration + ")";
    }

    /** {@code waitFor(trigger(), timeout: 30s, ...)}. Other settings use waitUntilTrigger(...). */
    private String waitForCall(JsonNode item) {
        if (!onlyHas(item, WAIT_FOR_FIELDS)) {
            return decompilation.items().mappedCall(item, CommandCategory.ACTION, true);
        }
        List<String> triggers = new ArrayList<>();
        item.path("triggersToWaitFor").forEach(trigger -> triggers.add(decompilation.triggerCall(trigger)));
        if (triggers.isEmpty()) {
            throw new UnrepresentableException("waitFor with no trigger");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(triggers.size() == 1 ? triggers.get(0) : "[" + String.join(", ", triggers) + "]");
        if (item.path("timeoutEnabled").asBoolean()) {
            arguments.add("timeout: " + item.path("timeoutSeconds").asLong(60) + "s");
        }
        if (item.has("continueOnTimeout") && !item.path("continueOnTimeout").asBoolean(true)) {
            arguments.add("continueOnTimeout: false");
        }
        if (item.path("booleanVariableName").isTextual() && !item.path("booleanVariableName").asText().isEmpty()) {
            arguments.add("result: " + decompilation.variables().registerName(item.path("booleanVariableName").asText())
                    + decompilation.keySuffix(item.get("booleanVarDictionaryKeys")));
        }
        return "waitFor(" + String.join(", ", arguments) + ")";
    }

    private String callBlock(JsonNode item) {
        String arguments = ActionBlockCalls.arguments(decompilation, item);
        boolean async = item.path("continueActionsWithoutWaiting").asBoolean();
        return "callBlock(" + arguments + (async ? ", async: true" : "") + ")";
    }

    /** {@code confirm(title: ..., text: ...)} for an If/ElseIfConfirmedThenAction. */
    private String confirm(JsonNode item) {
        List<String> arguments = new ArrayList<>();
        addText(arguments, item, "title", "title");
        addText(arguments, item, "text", "text");
        addText(arguments, item, "positiveText", "yes");
        addText(arguments, item, "negativeText", "no");
        if (item.path("cancelAfterTimeout").asBoolean()) {
            arguments.add("timeout: " + item.path("timeoutSeconds").asLong(30) + "s");
        }
        if (item.path("quitOnBackPressed").asBoolean()) {
            arguments.add("quitOnBack: true");
        }
        return "confirm(" + String.join(", ", arguments) + ")";
    }

    private void addText(List<String> arguments, JsonNode item, String field, String name) {
        if (item.path(field).isTextual()) {
            arguments.add(name + ": " + decompilation.textLiteral(item.path(field).asText()));
        }
    }

    /** True when every field with a value is a statement field or one of {@code allowed}. */
    private static boolean onlyHas(JsonNode item, Set<String> allowed) {
        var fields = item.fields();
        while (fields.hasNext()) {
            var field = fields.next();
            if (!allowed.contains(field.getKey()) && !ItemDecompiler.STATEMENT_FIELDS.contains(field.getKey())
                    && !ItemDecompiler.isEmptyValue(field.getValue())) {
                return false;
            }
        }
        return true;
    }
}
