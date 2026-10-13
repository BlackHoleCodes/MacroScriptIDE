package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Writes one macro object (or action block) as a MacroScript file. */
final class MacroDecompiler {

    private static final String DEFAULT_CATEGORY = "Uncategorized";

    private final Decompilation decompilation;
    private final Mapping mapping;

    MacroDecompiler(Mapping mapping, List<String> warnings) {
        this.mapping = mapping;
        this.decompilation = new Decompilation(mapping, warnings);
    }

    String decompile(JsonNode macro) {
        List<String> declarations = localDeclarations(macro.path("localVariables"));
        assignLabels(macro);

        // The blocks are written first, because they find the global variables that the
        // variables block must declare.
        SourceWriter triggers = triggersBlock(macro.path("m_triggerList"));
        SourceWriter constraints = constraintsBlock(macro.path("m_constraintList"), macro.path("m_isOrCondition").asBoolean());
        SourceWriter actions = new SourceWriter();
        new ActionDecompiler(decompilation).write(macro.path("m_actionList"), actions);
        decompilation.variables().globals().forEach((name, type) -> declarations.add("global " + type.keyword() + " "
                + name + ";" + (decompilation.variables().isTypeGuessed(name) ? " // check the type: the export does not say it" : "")));

        SourceWriter out = new SourceWriter();
        writeAnnotations(macro, out);
        writeBlock(out, "variables", declarations);
        boolean actionBlock = macro.path("isActionBlock").asBoolean();
        if (!actionBlock) {
            out.blankLine().open("triggers {").append(triggers).close("}");
        }
        if (!constraints.isEmpty()) {
            out.blankLine().open("constraints {").append(constraints).close("}");
        }
        out.blankLine().open("actions {").append(actions).close("}");
        return out.toString();
    }

    // ----- annotations -------------------------------------------------------------------

    private void writeAnnotations(JsonNode macro, SourceWriter out) {
        if (macro.path("isActionBlock").asBoolean()) {
            out.line("@actionBlock");
        }
        out.line("@name(" + StringLiterals.quote(macro.path("m_name").asText("Untitled")) + ")");
        out.line("@description(" + StringLiterals.quote(macro.path("m_description").asText("")) + ")");
        String category = macro.path("m_category").asText(DEFAULT_CATEGORY);
        if (!category.isEmpty() && !category.equals(DEFAULT_CATEGORY)) {
            out.line("@category(" + StringLiterals.quote(category) + ")");
        }
        if (!macro.path("m_enabled").asBoolean(true)) {
            out.line("@enabled(false)");
        }
        if (macro.path("isFavourite").asBoolean()) {
            out.line("@favourite");
        }
        if (macro.path("m_headingColor").isIntegralNumber() && macro.path("m_headingColor").asLong() != 0) {
            out.line("@headingColor(" + String.format("#%08X", macro.path("m_headingColor").asLong() & 0xFFFFFFFFL) + ")");
        }
        if (macro.path("m_excludeLog").asBoolean()) {
            out.line("@noLog");
        }
        if (macro.path("loggingLevel").asInt(0) == 1) {
            out.line("@logLevel(TRIGGERS_ONLY)");
        }
    }

    // ----- variables ---------------------------------------------------------------------

    private List<String> localDeclarations(JsonNode localVariables) {
        List<String> declarations = new ArrayList<>();
        for (JsonNode variable : localVariables) {
            String name = variable.path("m_name").asText();
            VarType type = VarType.fromTypeCode(variable.path("m_type").asInt(2)).orElse(VarType.STRING);
            decompilation.variables().addLocal(name, type);
            StringBuilder declaration = new StringBuilder();
            if (variable.path("isSecure").asBoolean()) {
                declaration.append("secure ");
            }
            if (variable.path("supportsInput").asBoolean()) {
                declaration.append("in ");
            }
            if (variable.path("supportsOutput").asBoolean()) {
                declaration.append("out ");
            }
            declaration.append(type.keyword()).append(' ').append(name);
            String initial;
            try {
                initial = initialValue(variable, type);
            } catch (UnrepresentableException e) {
                decompilation.warn("The initial value of '" + name + "' was left out: " + e.getMessage());
                initial = null;
            }
            if (initial != null) {
                declaration.append(" = ").append(initial);
            }
            declarations.add(declaration.append(';').toString());
        }
        return declarations;
    }

    /** The initial value, or null when it is the default for the type. */
    private static String initialValue(JsonNode variable, VarType type) {
        return switch (type) {
            case BOOL -> variable.path("m_booleanValue").asBoolean() ? "true" : null;
            case INT -> variable.path("m_intValue").asLong(0) != 0 ? Long.toString(variable.path("m_intValue").asLong()) : null;
            case DECIMAL -> variable.path("m_decimalValue").asDouble(0) != 0
                    ? JsonSource.decimal(variable.path("m_decimalValue").asDouble()) : null;
            case STRING -> variable.path("m_stringValue").asText("").isEmpty() ? null
                    : StringLiterals.quote(variable.path("m_stringValue").asText());
            case DICT, ARRAY -> {
                JsonNode entries = variable.path("dictionary").path("entries");
                yield entries.isArray() && !entries.isEmpty() ? DictionaryLiterals.literal(variable.path("dictionary")) : null;
            }
            case COLOR -> null;
        };
    }

    // ----- triggers and constraints ------------------------------------------------------

    /**
     * Finds the triggers that {@code triggeredBy(...)} refers to and gives each a label,
     * taken from the trigger name in the constraint when that is a usable name.
     */
    private void assignLabels(JsonNode macro) {
        Map<Long, String> wantedNames = new HashMap<>();
        collectTriggerReferences(macro, wantedNames);
        Set<String> used = new HashSet<>(mapping.reservedWords());
        int counter = 1;
        for (JsonNode trigger : macro.path("m_triggerList")) {
            long guid = trigger.path("m_SIGUID").asLong();
            if (!wantedNames.containsKey(guid)) {
                continue;
            }
            String label = wantedNames.get(guid);
            if (label == null || !label.matches("[A-Za-z_][A-Za-z0-9_]*") || used.contains(label)
                    || decompilation.variables().isDeclared(label)) {
                do {
                    label = "trigger" + counter++;
                } while (used.contains(label));
            }
            used.add(label);
            decompilation.triggerLabels().put(guid, label);
        }
    }

    private static void collectTriggerReferences(JsonNode node, Map<Long, String> references) {
        if (node.isObject()) {
            if ("TriggerThatInvokedConstraint".equals(node.path("m_classType").asText())) {
                references.putIfAbsent(node.path("m_siGuidThatInvoked").asLong(), node.path("m_triggerName").asText(null));
            }
            node.forEach(child -> collectTriggerReferences(child, references));
        } else if (node.isArray()) {
            node.forEach(child -> collectTriggerReferences(child, references));
        }
    }

    private SourceWriter triggersBlock(JsonNode triggers) {
        SourceWriter out = new SourceWriter();
        for (JsonNode trigger : triggers) {
            MetaLines.write(trigger, out);
            StringBuilder statement = new StringBuilder(decompilation.items().call(trigger, CommandCategory.TRIGGER, true));
            String label = decompilation.triggerLabels().get(trigger.path("m_SIGUID").asLong());
            if (label != null) {
                statement.append(" as ").append(label);
            }
            JsonNode constraints = trigger.path("m_constraintList");
            if (constraints.isArray() && !constraints.isEmpty()) {
                statement.append(" when (")
                        .append(decompilation.conditions().list(constraints, trigger.path("m_isOrCondition").asBoolean()))
                        .append(')');
            }
            out.line(statement.append(';').toString());
        }
        return out;
    }

    private SourceWriter constraintsBlock(JsonNode constraints, boolean isOr) {
        SourceWriter out = new SourceWriter();
        if (!constraints.isArray() || constraints.isEmpty()) {
            return out;
        }
        if (isOr && constraints.size() > 1) {
            out.line(decompilation.conditions().list(constraints, true) + ";");
            return out;
        }
        for (JsonNode constraint : constraints) {
            MetaLines.write(constraint, out);
            out.line(decompilation.conditions().statement(constraint) + ";");
        }
        return out;
    }

    private static void writeBlock(SourceWriter out, String name, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        out.blankLine().open(name + " {");
        lines.forEach(out::line);
        out.close("}");
    }
}
