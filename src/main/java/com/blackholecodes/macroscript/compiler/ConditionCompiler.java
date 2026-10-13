package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.ConstraintStatement;
import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.Operator;
import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Compiles conditions to constraint items. Conditions are used in the constraints block,
 * in if, else if, while and do-while, and after {@code when}.
 *
 * <p>The top level of a condition becomes a flat list: {@code a && b} is a list with AND,
 * {@code a || b} is a list with OR (m_isOrCondition). Deeper logic becomes LogicConstraint
 * items.
 */
public final class ConditionCompiler {

    private static final String VARIABLE_CONSTRAINT = "MacroDroidVariableConstraint";
    private static final String COMPARE_CONSTRAINT = "CompareValueConstraint";
    private static final String LOGIC_CONSTRAINT = "LogicConstraint";

    // LogicConstraint m_option values.
    private static final int LOGIC_AND = 0;
    private static final int LOGIC_OR = 1;
    private static final int LOGIC_XOR = 2;
    private static final int LOGIC_NOT = 3;

    // MacroDroidVariableConstraint m_stringComparisonType values.
    private static final int STRING_EQUALS = 0;
    private static final int STRING_CONTAINS = 1;
    private static final int STRING_EXCLUDES = 2;

    // CompareValueConstraint type and comparisonType values.
    private static final int COMPARE_TYPE_BOOLEAN = 0;
    private static final int COMPARE_TYPE_INTEGER = 1;
    private static final int COMPARE_TYPE_STRING = 2;
    private static final int COMPARE_TYPE_DECIMAL = 3;
    private static final int COMPARE_TYPE_DICTIONARY = 4;
    private static final int COMPARE_TYPE_ARRAY = 5;
    private static final int COMPARE_EQUALS = 0;
    private static final int COMPARE_NOT_EQUALS = 1;
    private static final int COMPARE_LESS = 2;
    private static final int COMPARE_GREATER = 3;
    private static final int COMPARE_CONTAINS = 4;
    private static final int COMPARE_EXCLUDES = 5;
    private static final int COMPARE_CONTAINS_KEY = 6;
    private static final int COMPARE_CONTAINS_VALUE = 8;

    private final Compilation compilation;

    ConditionCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    // ----- entry points ------------------------------------------------------------------

    /** Compiles a whole condition, for example the one in {@code if (...)}. */
    public Optional<ConstraintList> compile(Expr condition) {
        if (isLogical(condition, Operator.AND)) {
            return list(flatten(condition, Operator.AND), false);
        }
        if (isLogical(condition, Operator.OR)) {
            return list(flatten(condition, Operator.OR), true);
        }
        return single(condition).map(item -> new ConstraintList(List.of(item), false));
    }

    /**
     * Compiles the constraints block. All statements must be true. A block with one
     * statement that is a flat OR becomes a flat list with m_isOrCondition.
     */
    public Optional<ConstraintList> compileBlock(List<ConstraintStatement> statements) {
        if (statements.size() == 1) {
            return compile(statements.get(0).condition()).map(list -> withMeta(list, statements.get(0)));
        }
        List<ObjectNode> items = new ArrayList<>();
        boolean ok = true;
        for (ConstraintStatement statement : statements) {
            List<Expr> parts = isLogical(statement.condition(), Operator.AND)
                    ? flatten(statement.condition(), Operator.AND)
                    : List.of(statement.condition());
            for (Expr part : parts) {
                Optional<ObjectNode> item = single(part);
                ok &= item.isPresent();
                item.ifPresent(node -> {
                    Items.applyMeta(node, statement.meta());
                    items.add(node);
                });
            }
        }
        return ok ? Optional.of(new ConstraintList(List.copyOf(items), false)) : Optional.empty();
    }

    private static ConstraintList withMeta(ConstraintList list, ConstraintStatement statement) {
        list.items().forEach(item -> Items.applyMeta(item, statement.meta()));
        return list;
    }

    private Optional<ConstraintList> list(List<Expr> parts, boolean isOr) {
        List<ObjectNode> items = new ArrayList<>();
        boolean ok = true;
        for (Expr part : parts) {
            Optional<ObjectNode> item = single(part);
            ok &= item.isPresent();
            item.ifPresent(items::add);
        }
        return ok ? Optional.of(new ConstraintList(List.copyOf(items), isOr)) : Optional.empty();
    }

    // ----- one constraint ----------------------------------------------------------------

    /** Compiles a condition to exactly one constraint item. */
    public Optional<ObjectNode> single(Expr condition) {
        return switch (condition) {
            case Expr.Binary b when b.operator() == Operator.AND -> logic(LOGIC_AND, flatten(b, Operator.AND));
            case Expr.Binary b when b.operator() == Operator.OR -> logic(LOGIC_OR, flatten(b, Operator.OR));
            case Expr.Binary b when b.operator().isComparison() -> comparison(b.operator(), b.left(), b.right(), b);
            case Expr.Not not -> negated(not.operand(), not);
            case Expr.Call call -> call(call);
            case Expr.MethodCall method -> method(method, false);
            case Expr.Name n -> booleanVariable(n, true);
            case Expr.Index i -> booleanVariable(i, true);
            case Expr.Cast c -> booleanVariable(c, true);
            case Expr.BoolLiteral b -> {
                compilation.error(b.range(), "A condition cannot be the literal " + b.value() + ".");
                yield Optional.empty();
            }
            default -> {
                compilation.error(condition.range(), "This is not a condition. Use a constraint call, a comparison "
                        + "such as count > 5, or a bool variable.");
                yield Optional.empty();
            }
        };
    }

    private Optional<ObjectNode> negated(Expr operand, Expr.Not not) {
        return switch (operand) {
            case Expr.Not inner -> single(inner.operand());
            case Expr.Name n -> booleanVariable(n, false);
            case Expr.Index i -> booleanVariable(i, false);
            case Expr.Cast c -> booleanVariable(c, false);
            case Expr.MethodCall method -> method(method, true);
            case Expr.Binary b when b.operator().isComparison() ->
                    comparison(b.operator().negated(), b.left(), b.right(), not);
            case Expr.Call call when call.name().equals("triggeredBy") -> triggeredBy(call, true);
            default -> single(operand).map(child -> logicItem(LOGIC_NOT, List.of(child)));
        };
    }

    private Optional<ObjectNode> call(Expr.Call call) {
        return switch (call.name()) {
            case "xor" -> {
                if (call.arguments().size() < 2 || call.arguments().stream().anyMatch(a -> a.name() != null)) {
                    compilation.error(call.range(), "xor needs two or more conditions: xor(a, b).");
                    yield Optional.empty();
                }
                yield logic(LOGIC_XOR, call.arguments().stream().map(Expr.Argument::value).toList());
            }
            case "triggeredBy" -> triggeredBy(call, false);
            case "confirm" -> {
                compilation.error(call.range(), "confirm() must be the whole condition of an if or else if.");
                yield Optional.empty();
            }
            case "raw" -> compilation.helpers().raw(call);
            default -> compilation.commands().compileCall(CommandCategory.CONSTRAINT, call);
        };
    }

    private Optional<ObjectNode> logic(int option, List<Expr> operands) {
        List<ObjectNode> children = new ArrayList<>();
        boolean ok = true;
        for (Expr operand : operands) {
            Optional<ObjectNode> child = single(operand);
            ok &= child.isPresent();
            child.ifPresent(children::add);
        }
        return ok ? Optional.of(logicItem(option, children)) : Optional.empty();
    }

    private static ObjectNode logicItem(int option, List<ObjectNode> children) {
        ObjectNode item = Items.create(LOGIC_CONSTRAINT);
        item.put("m_option", option);
        ArrayNode array = item.putArray("m_childConstraints");
        children.forEach(array::add);
        return item;
    }

    private Optional<ObjectNode> triggeredBy(Expr.Call call, boolean not) {
        if (call.arguments().size() != 1 || !(call.arguments().get(0).value() instanceof Expr.Name label)) {
            compilation.error(call.range(), "triggeredBy needs one trigger label: triggeredBy(morning).");
            return Optional.empty();
        }
        Optional<TriggerLabel> found = compilation.scope().label(label.name());
        if (found.isEmpty()) {
            String known = compilation.scope().labelNames().isEmpty()
                    ? " No trigger has a label. Add one with: trigger(...) as " + label.name() + ";"
                    : Suggestions.didYouMean(label.name(), compilation.scope().labelNames());
            compilation.error(label.range(), "No trigger has the label '" + label.name() + "'." + known);
            return Optional.empty();
        }
        ObjectNode item = Items.create("TriggerThatInvokedConstraint");
        item.put("m_siGuidThatInvoked", found.get().guid());
        item.put("m_triggerName", found.get().name());
        if (not) {
            item.put("m_not", true);
        }
        return Optional.of(item);
    }

    // ----- variable conditions -----------------------------------------------------------

    /** {@code flag} or {@code !flag}: a bool variable or entry is true or false. */
    private Optional<ObjectNode> booleanVariable(Expr expr, boolean expected) {
        VarType castType = expr instanceof Expr.Cast cast ? cast.type() : null;
        Optional<Reference> reference = compilation.references().resolve(expr);
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        if (!(reference.get() instanceof Reference.Variable variable)) {
            compilation.error(expr.range(), "This value is not a bool. Compare it, for example sys.battery_int > 50.");
            return Optional.empty();
        }
        VarType type = variable.isEntry() ? (castType != null ? castType : VarType.BOOL) : variable.symbol().type();
        if (type != VarType.BOOL) {
            compilation.error(expr.range(), "'" + variable.symbol().name() + "' is " + describe(type)
                    + ", so it cannot be a condition on its own. Compare it, for example "
                    + variable.symbol().name() + " == 5.");
            return Optional.empty();
        }
        return variableItem(variable, type).map(item -> {
            item.put("m_booleanValue", expected);
            return item;
        });
    }

    private Optional<ObjectNode> comparison(Operator op, Expr left, Expr right, Expr whole) {
        // Put the reference on the left: 5 < count is count > 5.
        if (!ReferenceResolver.looksLikeReference(left) && !isInterpolatedString(left)
                && (ReferenceResolver.looksLikeReference(right) || isInterpolatedString(right))) {
            return comparison(op.mirrored(), right, left, whole);
        }
        if (isInterpolatedString(left)) {
            return compilation.magicText().text((Expr.StringLiteral) left)
                    .flatMap(text -> compareValue(op, text, right, COMPARE_TYPE_STRING));
        }
        if (!ReferenceResolver.looksLikeReference(left)) {
            compilation.error(whole.range(), "One side of a comparison must be a variable or a sys value.");
            return Optional.empty();
        }
        VarType castType = left instanceof Expr.Cast cast ? cast.type() : null;
        Optional<Reference> reference = compilation.references().resolve(left);
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        if (reference.get() instanceof Reference.Variable variable) {
            return variableComparison(op, variable, castType, right, whole);
        }
        Optional<String> leftText = compilation.magicText().reference(reference.get());
        VarType knownType = castType != null ? castType : reference.get().knownType();
        int compareType = compareTypeFor(knownType != null ? knownType : literalType(right), op);
        return leftText.flatMap(text -> compareValue(op, text, right, compareType));
    }

    private Optional<ObjectNode> variableComparison(Operator op, Reference.Variable variable, VarType castType,
                                                    Expr right, Expr whole) {
        VarType type;
        if (variable.isEntry()) {
            type = castType != null ? castType : literalType(right);
            if (type == null) {
                compilation.error(whole.range(), "The type of this entry is not known. Add a cast, for example (int) "
                        + variable.symbol().name() + "[\"key\"] > 5.");
                return Optional.empty();
            }
        } else {
            type = variable.symbol().type();
        }
        return switch (type) {
            case BOOL -> boolComparison(op, variable, right, whole);
            case INT, DECIMAL -> numberComparison(op, variable, type, right, whole);
            case STRING -> stringComparison(op, variable, right, whole);
            case DICT, ARRAY -> {
                compilation.error(whole.range(), "A " + type.keyword() + " cannot be compared. Use .containsKey(k) or "
                        + ".containsValue(v), or compare one entry.");
                yield Optional.empty();
            }
            case COLOR -> {
                compilation.error(whole.range(), "MacroDroid cannot compare color variables.");
                yield Optional.empty();
            }
        };
    }

    private Optional<ObjectNode> boolComparison(Operator op, Reference.Variable variable, Expr right, Expr whole) {
        if (!(right instanceof Expr.BoolLiteral bool) || (op != Operator.EQUAL && op != Operator.NOT_EQUAL)) {
            compilation.error(whole.range(), "A bool can only be compared with == true or == false. "
                    + "You can also write the variable on its own: if (flag).");
            return Optional.empty();
        }
        boolean expected = bool.value() == (op == Operator.EQUAL);
        return variableItem(variable, VarType.BOOL).map(item -> {
            item.put("m_booleanValue", expected);
            return item;
        });
    }

    private Optional<ObjectNode> numberComparison(Operator op, Reference.Variable variable, VarType type,
                                                  Expr right, Expr whole) {
        if (op == Operator.GREATER_OR_EQUAL || op == Operator.LESS_OR_EQUAL) {
            // MacroDroid has no >= or <=. For an int literal, move the bound by one.
            if (type == VarType.INT && right instanceof Expr.IntLiteral number) {
                long shifted = op == Operator.GREATER_OR_EQUAL ? number.value() - 1 : number.value() + 1;
                Operator strict = op == Operator.GREATER_OR_EQUAL ? Operator.GREATER : Operator.LESS;
                return numberComparison(strict, variable, type, new Expr.IntLiteral(shifted, number.range()), whole);
            }
            // Otherwise write (x > y || x == y).
            Operator strict = op == Operator.GREATER_OR_EQUAL ? Operator.GREATER : Operator.LESS;
            Optional<ObjectNode> strictItem = numberComparison(strict, variable, type, right, whole);
            Optional<ObjectNode> equalItem = numberComparison(Operator.EQUAL, variable, type, right, whole);
            if (strictItem.isEmpty() || equalItem.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(logicItem(LOGIC_OR, List.of(strictItem.get(), equalItem.get())));
        }
        Optional<ObjectNode> found = variableItem(variable, type);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = found.get();
        switch (right) {
            case Expr.IntLiteral number when type == VarType.INT -> item.put("m_intValue", number.value());
            case Expr.IntLiteral number -> item.put("m_doubleValue", (double) number.value());
            case Expr.DecimalLiteral number when type == VarType.DECIMAL -> item.put("m_doubleValue", number.value());
            case Expr.DecimalLiteral number -> {
                compilation.error(right.range(), "An int cannot be compared with a decimal number.");
                return Optional.empty();
            }
            default -> {
                if (!writeNumberOperand(item, right)) {
                    return Optional.empty();
                }
            }
        }
        switch (op) {
            case NOT_EQUAL -> item.put("m_intNotEqual", true);
            case GREATER -> item.put("m_intGreaterThan", true);
            case LESS -> item.put("m_intLessThan", true);
            default -> {
                // EQUAL is the default comparison and has no flag.
            }
        }
        return Optional.of(item);
    }

    /** The right side of a number comparison: another variable, or an expression. */
    private boolean writeNumberOperand(ObjectNode item, Expr right) {
        if (ReferenceResolver.looksLikeReference(right) && !(right instanceof Expr.SysValue)) {
            Optional<Reference> reference = compilation.references().resolve(right);
            if (reference.isEmpty()) {
                return false;
            }
            if (reference.get() instanceof Reference.Variable other) {
                item.put("m_intCompareVariable", true);
                item.set("m_otherValueToCompare", other.symbol().toReference());
                if (other.isEntry()) {
                    Optional<List<String>> keys = DictionaryKeys.render(compilation, other.keys());
                    if (keys.isEmpty()) {
                        return false;
                    }
                    item.set("compareVarDictionaryKeys", DictionaryKeys.keysObject(keys.get()));
                }
                return true;
            }
        }
        Optional<String> expression = compilation.magicText().arithmetic(right);
        expression.ifPresent(text -> item.put("m_expression", text));
        return expression.isPresent();
    }

    private Optional<ObjectNode> stringComparison(Operator op, Reference.Variable variable, Expr right, Expr whole) {
        if (op != Operator.EQUAL && op != Operator.NOT_EQUAL) {
            compilation.error(whole.range(), "Strings can only be compared with == and !=. "
                    + "Use .contains(...) or .matches(...) for other checks.");
            return Optional.empty();
        }
        Optional<String> text = compilation.magicText().textValue(right);
        if (text.isEmpty()) {
            return Optional.empty();
        }
        return variableItem(variable, VarType.STRING).map(item -> {
            item.put("m_stringValue", text.get());
            item.put("m_stringComparisonType", STRING_EQUALS);
            item.put("m_stringEqual", op == Operator.EQUAL);
            item.put("checkCase", true);
            return item;
        });
    }

    /** {@code s.contains("a")}, {@code d.containsKey("k")} and the other methods. */
    private Optional<ObjectNode> method(Expr.MethodCall method, boolean negate) {
        if (method.arguments().size() != 1 || method.arguments().get(0).name() != null) {
            compilation.error(method.range(), "." + method.method() + "(...) needs one value.");
            return Optional.empty();
        }
        Expr argument = method.arguments().get(0).value();
        Expr target = method.target();
        if (isInterpolatedString(target)) {
            return compilation.magicText().text((Expr.StringLiteral) target)
                    .flatMap(text -> compareValueMethod(method, text, argument, negate));
        }
        if (!ReferenceResolver.looksLikeReference(target)) {
            compilation.error(target.range(), "." + method.method() + "(...) needs a variable or a sys value in front.");
            return Optional.empty();
        }
        VarType castType = target instanceof Expr.Cast cast ? cast.type() : null;
        Optional<Reference> reference = compilation.references().resolve(target);
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        if (reference.get() instanceof Reference.Variable variable) {
            VarType type = variable.isEntry() ? (castType != null ? castType : VarType.STRING) : variable.symbol().type();
            if (type.isCollection() || method.method().startsWith("containsKey") || method.method().startsWith("containsValue")) {
                return collectionMethod(method, variable, type, argument, negate);
            }
            if (type == VarType.STRING) {
                return stringMethod(method, variable, argument, negate);
            }
            compilation.error(method.range(), "." + method.method() + "(...) needs a string, but '"
                    + variable.symbol().name() + "' is " + describe(type) + ".");
            return Optional.empty();
        }
        return compilation.magicText().reference(reference.get())
                .flatMap(text -> compareValueMethod(method, text, argument, negate));
    }

    private Optional<ObjectNode> stringMethod(Expr.MethodCall method, Reference.Variable variable, Expr argument,
                                              boolean negate) {
        Optional<String> text = compilation.magicText().textValue(argument);
        if (text.isEmpty()) {
            return Optional.empty();
        }
        Optional<ObjectNode> found = variableItem(variable, VarType.STRING);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = found.get();
        item.put("m_stringValue", text.get());
        switch (method.method()) {
            case "contains", "containsIgnoreCase" -> {
                item.put("m_stringComparisonType", negate ? STRING_EXCLUDES : STRING_CONTAINS);
                item.put("checkCase", method.method().equals("contains"));
            }
            case "equalsIgnoreCase" -> {
                item.put("m_stringComparisonType", STRING_EQUALS);
                item.put("m_stringEqual", !negate);
                item.put("checkCase", false);
            }
            case "matches" -> {
                item.put("m_stringComparisonType", STRING_EQUALS);
                item.put("m_stringEqual", !negate);
                item.put("enableRegex", true);
                item.put("checkCase", true);
            }
            default -> {
                unknownMethod(method);
                return Optional.empty();
            }
        }
        return Optional.of(item);
    }

    private Optional<ObjectNode> collectionMethod(Expr.MethodCall method, Reference.Variable variable, VarType type,
                                                  Expr argument, boolean negate) {
        int comparison;
        switch (method.method()) {
            case "containsKey" -> comparison = COMPARE_CONTAINS_KEY;
            case "containsValue" -> comparison = COMPARE_CONTAINS_VALUE;
            default -> {
                compilation.error(method.range(), "A " + type.keyword() + " has the methods .containsKey(k) and "
                        + ".containsValue(v).");
                return Optional.empty();
            }
        }
        if (!type.isCollection()) {
            compilation.error(method.range(), "." + method.method() + "(...) needs a dict or array.");
            return Optional.empty();
        }
        Optional<String> collection = compilation.magicText().reference(variable);
        Optional<String> value = compilation.magicText().textValue(argument);
        if (collection.isEmpty() || value.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = Items.create(COMPARE_CONSTRAINT);
        item.put("type", type == VarType.ARRAY ? COMPARE_TYPE_ARRAY : COMPARE_TYPE_DICTIONARY);
        item.put("value1", collection.get());
        item.put("value2", value.get());
        // The "excludes" form of each comparison is the next number.
        item.put("comparisonType", negate ? comparison + 1 : comparison);
        return Optional.of(item);
    }

    /** A string method on a sys value, a loop alias or an interpolated string. */
    private Optional<ObjectNode> compareValueMethod(Expr.MethodCall method, String value1, Expr argument, boolean negate) {
        Optional<String> value2 = compilation.magicText().textValue(argument);
        if (value2.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = Items.create(COMPARE_CONSTRAINT);
        item.put("type", COMPARE_TYPE_STRING);
        item.put("value1", value1);
        item.put("value2", value2.get());
        switch (method.method()) {
            case "contains", "containsIgnoreCase" -> {
                item.put("comparisonType", negate ? COMPARE_EXCLUDES : COMPARE_CONTAINS);
                setIgnoreCase(item, method.method().equals("containsIgnoreCase"));
            }
            case "equalsIgnoreCase" -> {
                item.put("comparisonType", negate ? COMPARE_NOT_EQUALS : COMPARE_EQUALS);
                setIgnoreCase(item, true);
            }
            case "matches" -> {
                item.put("comparisonType", negate ? COMPARE_NOT_EQUALS : COMPARE_EQUALS);
                item.put("enableRegex", true);
                setIgnoreCase(item, false);
            }
            default -> {
                unknownMethod(method);
                return Optional.empty();
            }
        }
        return Optional.of(item);
    }

    /** CompareValueConstraint for a sys value, a loop alias or an interpolated string. */
    private Optional<ObjectNode> compareValue(Operator op, String value1, Expr right, int compareType) {
        if (op == Operator.GREATER_OR_EQUAL || op == Operator.LESS_OR_EQUAL) {
            Operator strict = op == Operator.GREATER_OR_EQUAL ? Operator.GREATER : Operator.LESS;
            Optional<ObjectNode> strictItem = compareValue(strict, value1, right, compareType);
            Optional<ObjectNode> equalItem = compareValue(Operator.EQUAL, value1, right, compareType);
            if (strictItem.isEmpty() || equalItem.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(logicItem(LOGIC_OR, List.of(strictItem.get(), equalItem.get())));
        }
        Optional<String> value2 = compilation.magicText().textValue(right);
        if (value2.isEmpty()) {
            return Optional.empty();
        }
        ObjectNode item = Items.create(COMPARE_CONSTRAINT);
        item.put("type", compareType);
        item.put("value1", value1);
        item.put("value2", value2.get());
        item.put("comparisonType", switch (op) {
            case EQUAL -> COMPARE_EQUALS;
            case NOT_EQUAL -> COMPARE_NOT_EQUALS;
            case LESS -> COMPARE_LESS;
            default -> COMPARE_GREATER;
        });
        if (compareType == COMPARE_TYPE_STRING) {
            setIgnoreCase(item, false);
        }
        return Optional.of(item);
    }

    /** ignoreCase is true by default, so it is written only when it is false. */
    private static void setIgnoreCase(ObjectNode item, boolean ignoreCase) {
        if (!ignoreCase) {
            item.put("ignoreCase", false);
        }
    }

    private void unknownMethod(Expr.MethodCall method) {
        compilation.error(method.range(), "Unknown method ." + method.method() + "(...). Strings have .contains, "
                + ".containsIgnoreCase, .equalsIgnoreCase and .matches. Dicts and arrays have .containsKey and .containsValue.");
    }

    // ----- helpers -----------------------------------------------------------------------

    /** A MacroDroidVariableConstraint for a variable, with the dictionary path of an entry. */
    private Optional<ObjectNode> variableItem(Reference.Variable variable, VarType entryType) {
        ObjectNode item = Items.create(VARIABLE_CONSTRAINT);
        item.set("m_variable", variable.symbol().toReference());
        if (variable.isEntry()) {
            Optional<List<String>> keys = DictionaryKeys.render(compilation, variable.keys());
            if (keys.isEmpty()) {
                return Optional.empty();
            }
            item.set("dictionaryKeys", DictionaryKeys.keysObject(keys.get()));
            item.put("dictionaryType", entryType.typeCode());
        }
        return Optional.of(item);
    }

    private static int compareTypeFor(VarType type, Operator op) {
        if (type == null) {
            return op == Operator.EQUAL || op == Operator.NOT_EQUAL ? COMPARE_TYPE_STRING : COMPARE_TYPE_INTEGER;
        }
        return switch (type) {
            case BOOL -> COMPARE_TYPE_BOOLEAN;
            case INT -> COMPARE_TYPE_INTEGER;
            case DECIMAL -> COMPARE_TYPE_DECIMAL;
            case DICT -> COMPARE_TYPE_DICTIONARY;
            case ARRAY -> COMPARE_TYPE_ARRAY;
            default -> COMPARE_TYPE_STRING;
        };
    }

    /** The type of a literal, or null for anything else. */
    private static VarType literalType(Expr expr) {
        return switch (expr) {
            case Expr.IntLiteral i -> VarType.INT;
            case Expr.DecimalLiteral d -> VarType.DECIMAL;
            case Expr.BoolLiteral b -> VarType.BOOL;
            case Expr.StringLiteral s -> VarType.STRING;
            case Expr.Cast c -> c.type();
            default -> null;
        };
    }

    private static boolean isInterpolatedString(Expr expr) {
        return expr instanceof Expr.StringLiteral s && s.hasInterpolation();
    }

    private static boolean isLogical(Expr expr, Operator op) {
        return expr instanceof Expr.Binary b && b.operator() == op;
    }

    /** {@code a && b && c} as the list [a, b, c]. */
    private static List<Expr> flatten(Expr expr, Operator op) {
        List<Expr> parts = new ArrayList<>();
        if (expr instanceof Expr.Binary b && b.operator() == op) {
            parts.addAll(flatten(b.left(), op));
            parts.addAll(flatten(b.right(), op));
        } else {
            parts.add(expr);
        }
        return parts;
    }

    private static String describe(VarType type) {
        return (type == VarType.INT ? "an " : "a ") + type.keyword();
    }
}
