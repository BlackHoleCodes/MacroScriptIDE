package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Writes constraint items as MacroScript conditions: variable comparisons, sys value
 * comparisons, {@code triggeredBy(...)}, {@code &&}, {@code ||}, {@code !} and constraint calls.
 * This is the reverse of the compiler's ConditionCompiler.
 */
final class ConditionDecompiler {

    /** How tightly a condition binds, so parentheses are added only where they are needed. */
    private enum Precedence { OR, AND, ATOM }

    /** One condition as source text. */
    private record Condition(String text, Precedence precedence) {

        String wrappedFor(Precedence context) {
            return precedence.ordinal() < context.ordinal() ? "(" + text + ")" : text;
        }
    }

    private static final String[] COMPARE_OPERATORS = {"==", "!=", "<", ">"};

    private final Decompilation decompilation;

    ConditionDecompiler(Decompilation decompilation) {
        this.decompilation = decompilation;
    }

    /** A list of constraints joined with AND, or with OR when {@code isOr} is true. */
    String list(JsonNode constraints, boolean isOr) {
        List<String> parts = new ArrayList<>();
        for (JsonNode constraint : constraints) {
            parts.add(item(constraint).wrappedFor(isOr ? Precedence.AND : Precedence.ATOM));
        }
        return String.join(isOr ? " || " : " && ", parts);
    }

    /** One constraint as a statement of the constraints block. */
    String statement(JsonNode constraint) {
        return item(constraint).text();
    }

    private Condition item(JsonNode constraint) {
        try {
            return switch (constraint.path("m_classType").asText()) {
                case "LogicConstraint" -> logic(constraint);
                case "TriggerThatInvokedConstraint" -> triggeredBy(constraint);
                case "MacroDroidVariableConstraint" -> variable(constraint);
                case "CompareValueConstraint" -> compareValue(constraint);
                default -> atom(decompilation.items().call(constraint, CommandCategory.CONSTRAINT));
            };
        } catch (UnrepresentableException e) {
            return atom(decompilation.items().raw(constraint, false));
        }
    }

    private Condition logic(JsonNode constraint) {
        List<Condition> children = new ArrayList<>();
        constraint.path("m_childConstraints").forEach(child -> children.add(item(child)));
        if (children.isEmpty()) {
            throw new UnrepresentableException("A LogicConstraint with no children");
        }
        return switch (constraint.path("m_option").asInt()) {
            case 0 -> join(children, " && ", Precedence.AND);
            case 1 -> join(children, " || ", Precedence.OR);
            case 2 -> atom("xor(" + String.join(", ", children.stream().map(Condition::text).toList()) + ")");
            case 3 -> {
                if (children.size() != 1) {
                    throw new UnrepresentableException("A NOT with more than one child");
                }
                yield atom("!" + children.get(0).wrappedFor(Precedence.ATOM));
            }
            default -> throw new UnrepresentableException("Unknown LogicConstraint option");
        };
    }

    private static Condition join(List<Condition> children, String separator, Precedence precedence) {
        if (children.size() == 1) {
            throw new UnrepresentableException("A logic item with one child");
        }
        List<String> parts = children.stream().map(c -> c.wrappedFor(Precedence.values()[precedence.ordinal() + 1])).toList();
        return new Condition(String.join(separator, parts), precedence);
    }

    private Condition triggeredBy(JsonNode constraint) {
        String label = decompilation.triggerLabels().get(constraint.path("m_siGuidThatInvoked").asLong());
        if (label == null) {
            throw new UnrepresentableException("triggeredBy refers to a trigger that is not in the macro");
        }
        return atom((constraint.path("m_not").asBoolean() ? "!" : "") + "triggeredBy(" + label + ")");
    }

    // ----- MacroDroidVariableConstraint --------------------------------------------------

    private Condition variable(JsonNode constraint) {
        JsonNode reference = constraint.path("m_variable");
        String name = decompilation.variables().register(reference);
        JsonNode keys = constraint.get("dictionaryKeys");
        boolean entry = keys != null && keys.path("keys").isArray() && !keys.path("keys").isEmpty();
        VarType type = entry
                ? VarType.fromTypeCode(constraint.path("dictionaryType").asInt(-1)).orElseThrow(
                        () -> new UnrepresentableException("An entry comparison with no dictionaryType"))
                : VarType.fromTypeCode(reference.path("m_type").asInt(-1)).orElseThrow(
                        () -> new UnrepresentableException("A variable with no type"));
        String left = entry ? "(" + type.keyword() + ") " + name + decompilation.keySuffix(keys) : name;
        return atom(switch (type) {
            case BOOL -> (constraint.path("m_booleanValue").asBoolean(false) ? "" : "!") + left;
            case INT, DECIMAL -> left + " " + numberOperator(constraint) + " " + numberOperand(constraint, type);
            case STRING -> stringCondition(constraint, left);
            default -> throw new UnrepresentableException("A comparison of a " + type.keyword());
        });
    }

    private static String numberOperator(JsonNode constraint) {
        if (constraint.path("m_intNotEqual").asBoolean()) {
            return "!=";
        }
        if (constraint.path("m_intGreaterThan").asBoolean()) {
            return ">";
        }
        if (constraint.path("m_intLessThan").asBoolean()) {
            return "<";
        }
        return "==";
    }

    private String numberOperand(JsonNode constraint, VarType type) {
        if (constraint.path("m_intCompareVariable").asBoolean()) {
            String other = decompilation.variables().register(constraint.path("m_otherValueToCompare"));
            return other + decompilation.keySuffix(constraint.get("compareVarDictionaryKeys"));
        }
        String expression = constraint.path("m_expression").asText("");
        if (!expression.isEmpty()) {
            return "(" + decompilation.arithmetic().read(expression) + ")";
        }
        return type == VarType.INT
                ? Long.toString(constraint.path("m_intValue").asLong(0))
                : JsonSource.decimal(constraint.path("m_doubleValue").asDouble(0));
    }

    private String stringCondition(JsonNode constraint, String left) {
        String value = decompilation.textLiteral(constraint.path("m_stringValue").asText(""));
        int comparison = constraint.path("m_stringComparisonType").asInt(0);
        boolean equal = constraint.path("m_stringEqual").asBoolean(false);
        boolean checkCase = constraint.path("checkCase").asBoolean(false);
        boolean regex = constraint.path("enableRegex").asBoolean(false);
        return switch (comparison) {
            case 0 -> {
                if (regex) {
                    if (!checkCase) {
                        throw new UnrepresentableException("A case-insensitive regex");
                    }
                    yield (equal ? "" : "!") + left + ".matches(" + value + ")";
                }
                yield checkCase
                        ? left + (equal ? " == " : " != ") + value
                        : (equal ? "" : "!") + left + ".equalsIgnoreCase(" + value + ")";
            }
            case 1 -> left + (checkCase ? ".contains(" : ".containsIgnoreCase(") + value + ")";
            case 2 -> "!" + left + (checkCase ? ".contains(" : ".containsIgnoreCase(") + value + ")";
            default -> throw new UnrepresentableException("Unknown string comparison");
        };
    }

    // ----- CompareValueConstraint --------------------------------------------------------

    private Condition compareValue(JsonNode constraint) {
        int type = constraint.path("type").asInt(-1);
        int comparison = constraint.path("comparisonType").asInt(-1);
        String value1 = constraint.path("value1").asText("");
        String value2 = constraint.path("value2").asText("");
        if (type == 4 || type == 5) {
            return atom(collectionCondition(comparison, value1, value2));
        }
        boolean ignoreCase = constraint.path("ignoreCase").asBoolean(true);
        boolean regex = constraint.path("enableRegex").asBoolean(false);
        String left = compareValueLeft(value1, type);
        if (type == 2) {
            String right = decompilation.textLiteral(value2);
            return atom(switch (comparison) {
                case 0, 1 -> {
                    String negation = comparison == 1 ? "!" : "";
                    if (regex) {
                        yield negation + left + ".matches(" + right + ")";
                    }
                    yield ignoreCase ? negation + left + ".equalsIgnoreCase(" + right + ")"
                            : left + (comparison == 0 ? " == " : " != ") + right;
                }
                case 4 -> left + (ignoreCase ? ".containsIgnoreCase(" : ".contains(") + right + ")";
                case 5 -> "!" + left + (ignoreCase ? ".containsIgnoreCase(" : ".contains(") + right + ")";
                default -> throw new UnrepresentableException("Unsupported text comparison");
            });
        }
        if (comparison < 0 || comparison > 3) {
            throw new UnrepresentableException("Unsupported comparison");
        }
        return atom(left + " " + COMPARE_OPERATORS[comparison] + " " + compareValueRight(value2, type));
    }

    /** {@code d.containsKey("k")} and the other dict and array checks. */
    private String collectionCondition(int comparison, String value1, String value2) {
        String collection = decompilation.magicText().wholeReference(value1)
                .filter(name -> decompilation.variables().isDeclared(name.replaceAll("\\[.*", "")))
                .orElseThrow(() -> new UnrepresentableException("A dict check on something that is not a variable"));
        String method = switch (comparison) {
            case 6, 7 -> "containsKey";
            case 8, 9 -> "containsValue";
            default -> throw new UnrepresentableException("Unsupported dict comparison");
        };
        String negation = comparison == 7 || comparison == 9 ? "!" : "";
        return negation + collection + "." + method + "(" + decompilation.textLiteral(value2) + ")";
    }

    /**
     * The left side: a sys value or loop name, or a string with magic text. A variable is
     * written inside a string, because a bare variable compiles to a variable constraint.
     */
    private String compareValueLeft(String value1, int type) {
        Optional<String> reference = decompilation.magicText().wholeReference(value1);
        boolean isVariable = reference.isPresent() && decompilation.variables().isDeclared(reference.get().replaceAll("\\[.*", ""));
        if (reference.isPresent() && !isVariable) {
            return reference.get();
        }
        if (type == 2 && value1.contains("{")) {
            String literal = decompilation.textLiteral(value1);
            if (literal.contains("${")) {
                return literal;
            }
        }
        throw new UnrepresentableException("The left side of a value comparison cannot be written");
    }

    private String compareValueRight(String value2, int type) {
        switch (type) {
            case 0 -> {
                if (value2.equals("true") || value2.equals("false")) {
                    return value2;
                }
            }
            case 1 -> {
                if (value2.matches("-?\\d+")) {
                    return value2;
                }
            }
            case 3 -> {
                if (value2.matches("-?\\d+(\\.\\d+)?")) {
                    return JsonSource.decimal(Double.parseDouble(value2));
                }
            }
            default -> throw new UnrepresentableException("Unsupported value type");
        }
        throw new UnrepresentableException("The value " + value2 + " cannot be written for this type");
    }

    private static Condition atom(String text) {
        return new Condition(text, Precedence.ATOM);
    }
}
