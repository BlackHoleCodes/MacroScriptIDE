package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.AssignOp;
import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.Stmt;
import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Compiles assignments to SetVariableAction. The fields that are written depend on the
 * type of the variable and the form of the value, as listed in the language reference
 * under "Assignment".
 */
public final class AssignmentCompiler {

    /** prompt() argument names and the setVariable parameters they set. */
    private static final Map<String, String> PROMPT_ARGUMENTS = Map.of(
            "title", "userPromptTitle",
            "message", "userPromptMessage",
            "password", "userPromptPassword",
            "passwordToggle", "userPromptPasswordToggle",
            "emptyAtStart", "userPromptEmptyAtStart",
            "showCancel", "userPromptShowCancel",
            "stopAfterCancel", "userPromptStopAfterCancel");

    /** dictionaryOrArrayType when the target is a whole variable. */
    private static final int NOT_AN_ENTRY = -1;

    private final Compilation compilation;

    AssignmentCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    public Optional<ObjectNode> compile(Stmt.Assignment assignment) {
        Stmt.Target target = assignment.target();
        Optional<VariableSymbol> found = compilation.scope().variable(target.name());
        if (found.isEmpty()) {
            compilation.error(target.nameRange(), "'" + target.name() + "' is not declared. Declare it in the variables block.");
            return Optional.empty();
        }
        VariableSymbol symbol = found.get();
        ObjectNode item = Items.create(setVariableSpec().classType());
        item.set("m_variable", symbol.toReference());
        Items.applyMeta(item, assignment.meta());

        VarType type = symbol.type();
        if (target.isEntry()) {
            if (!symbol.type().isCollection()) {
                compilation.error(target.range(), "'" + symbol.name() + "' is a " + symbol.type().keyword()
                        + ", so it has no entries.");
                return Optional.empty();
            }
            type = entryType(assignment);
            if (type == null) {
                return Optional.empty();
            }
            Optional<List<String>> keys = DictionaryKeys.render(compilation, target.keys());
            if (keys.isEmpty()) {
                return Optional.empty();
            }
            item.set("dictionaryKeys", DictionaryKeys.list(keys.get()));
            item.put("dictionaryOrArrayType", type.typeCode());
        }

        boolean ok = switch (type) {
            case BOOL -> assignBool(item, assignment, symbol);
            case INT -> assignNumber(item, assignment, symbol, false);
            case DECIMAL -> assignNumber(item, assignment, symbol, true);
            case STRING -> assignString(item, assignment, symbol);
            case COLOR -> assignColor(item, assignment);
            case DICT, ARRAY -> assignCollection(item, assignment, symbol);
        };
        return ok ? Optional.of(item) : Optional.empty();
    }

    /** The type of the value written to a dict or array entry: from a cast or the value. */
    private VarType entryType(Stmt.Assignment assignment) {
        Expr value = assignment.value();
        if (value == null) {
            return VarType.INT;
        }
        VarType type = typeOf(value);
        if (type == null || !type.isScalar()) {
            compilation.error(value.range(), "The type of the new entry value is not known. Add a cast, for example "
                    + "d[\"k\"] = (int) other[\"k\"];");
            return null;
        }
        return type;
    }

    // ----- by type -----------------------------------------------------------------------

    private boolean assignBool(ObjectNode item, Stmt.Assignment assignment, VariableSymbol symbol) {
        Expr value = assignment.value();
        if (assignment.op() != AssignOp.ASSIGN) {
            return wrongOperator(assignment, "a bool");
        }
        switch (value) {
            case Expr.BoolLiteral bool -> item.put("m_newBooleanValue", bool.value());
            case Expr.Not not when isSameVariable(not.operand(), assignment.target()) -> item.put("m_booleanInvert", true);
            case Expr.Call call when call.name().equals("prompt") -> {
                return prompt(item, call);
            }
            case Expr.Name name -> {
                Optional<Reference.Variable> other = compilation.references().resolveVariable(name);
                if (other.isEmpty()) {
                    return false;
                }
                if (other.get().symbol().type() != VarType.BOOL) {
                    return typeMismatch(value, symbol, other.get().symbol().type());
                }
                item.set("m_otherBooleanVariable", other.get().symbol().toReference());
            }
            default -> {
                compilation.error(value.range(), "A bool can be set to true, false, !" + assignment.target().name()
                        + ", another bool variable or prompt(...).");
                return false;
            }
        }
        return true;
    }

    private boolean assignNumber(ObjectNode item, Stmt.Assignment assignment, VariableSymbol symbol, boolean decimal) {
        AssignOp op = assignment.op();
        Expr value = assignment.value();
        String self = selfMagicText(assignment);

        // x++, x += 1, x--, x -= 1 have their own fields for ints.
        if (!decimal && (op == AssignOp.INCREMENT || (op == AssignOp.ADD && isIntLiteral(value, 1)))) {
            item.put("m_intValueIncrement", true);
            return true;
        }
        if (!decimal && (op == AssignOp.DECREMENT || (op == AssignOp.SUBTRACT && isIntLiteral(value, 1)))) {
            item.put("m_intValueDecrement", true);
            return true;
        }
        if (op == AssignOp.INCREMENT || op == AssignOp.DECREMENT) {
            return expression(item, self + op.arithmetic().symbol() + "1");
        }
        if (op != AssignOp.ASSIGN) {
            Optional<String> right = compilation.magicText().arithmetic(value);
            if (right.isEmpty()) {
                return false;
            }
            // x *= a + b means x * (a + b).
            boolean needsParentheses = value instanceof Expr.Binary binary
                    && binary.operator().precedence() <= op.arithmetic().precedence();
            String rightText = needsParentheses ? "(" + right.get() + ")" : right.get();
            return expression(item, self + op.arithmetic().symbol() + rightText);
        }

        switch (value) {
            case Expr.IntLiteral number when !decimal -> item.put("m_newIntValue", number.value());
            case Expr.IntLiteral number -> item.put("m_newDoubleValue", (double) number.value());
            case Expr.DecimalLiteral number when decimal -> item.put("m_newDoubleValue", number.value());
            case Expr.DecimalLiteral number -> {
                return typeMismatch(value, symbol, VarType.DECIMAL);
            }
            case Expr.Call call when call.name().equals("random") -> {
                return random(item, call, decimal);
            }
            case Expr.Call call when call.name().equals("prompt") -> {
                return prompt(item, call);
            }
            default -> {
                VarType valueType = typeOf(value);
                if (valueType != null && valueType != VarType.INT && valueType != VarType.DECIMAL) {
                    return typeMismatch(value, symbol, valueType);
                }
                Optional<String> text = compilation.magicText().arithmetic(value);
                return text.isPresent() && expression(item, text.get());
            }
        }
        return true;
    }

    private boolean assignString(ObjectNode item, Stmt.Assignment assignment, VariableSymbol symbol) {
        Expr value = assignment.value();
        if (value instanceof Expr.Call call && call.name().equals("prompt") && assignment.op() == AssignOp.ASSIGN) {
            return prompt(item, call);
        }
        String prefix;
        if (assignment.op() == AssignOp.ASSIGN) {
            prefix = "";
        } else if (assignment.op() == AssignOp.ADD) {
            prefix = selfMagicText(assignment);
        } else {
            return wrongOperator(assignment, "a string");
        }
        VarType valueType = typeOf(value);
        if (valueType == VarType.BOOL || valueType == VarType.DICT || valueType == VarType.ARRAY) {
            return typeMismatch(value, symbol, valueType);
        }
        Optional<String> text = compilation.magicText().concatenation(value);
        text.ifPresent(t -> item.put("m_newStringValue", prefix + t));
        return text.isPresent();
    }

    private boolean assignColor(ObjectNode item, Stmt.Assignment assignment) {
        if (assignment.op() == AssignOp.ASSIGN && assignment.value() instanceof Expr.ColorLiteral color) {
            item.put("m_newStringValue", color.text());
            return true;
        }
        compilation.error(assignment.range(), "A color variable can only be set to a color literal, for example "
                + assignment.target().name() + " = #FF2196F3;");
        return false;
    }

    private boolean assignCollection(ObjectNode item, Stmt.Assignment assignment, VariableSymbol symbol) {
        boolean copiesVariable = assignment.value() instanceof Expr.Name || assignment.value() instanceof Expr.Index;
        if (assignment.op() == AssignOp.ASSIGN && copiesVariable) {
            Optional<Reference.Variable> other = compilation.references().resolveVariable(assignment.value());
            if (other.isEmpty()) {
                return false;
            }
            if (!other.get().symbol().type().isCollection()) {
                return typeMismatch(assignment.value(), symbol, other.get().symbol().type());
            }
            ObjectNode location = item.putObject("copyDictionaryLocation");
            location.put("variableName", other.get().symbol().name());
            if (other.get().isEntry()) {
                Optional<List<String>> keys = DictionaryKeys.render(compilation, other.get().keys());
                if (keys.isEmpty()) {
                    return false;
                }
                location.set("dictionaryKeys", DictionaryKeys.keysObject(keys.get()));
            }
            item.put("dictionaryOrArrayType", NOT_AN_ENTRY);
            return true;
        }
        compilation.error(assignment.range(), "A " + symbol.type().keyword() + " can be set to another "
                + symbol.type().keyword() + " variable, or one entry can be set: "
                + symbol.name() + "[\"key\"] = value;");
        return false;
    }

    // ----- helpers that give values ------------------------------------------------------

    /** {@code random(min, max)}: an int or decimal between the two values. */
    private boolean random(ObjectNode item, Expr.Call call, boolean decimal) {
        List<Expr.Argument> args = call.arguments();
        if (args.size() != 2 || args.stream().anyMatch(a -> a.name() != null)) {
            compilation.error(call.range(), "random needs two values: random(1, 10).");
            return false;
        }
        Expr min = args.get(0).value();
        Expr max = args.get(1).value();
        item.put("m_intRandom", true);
        if (decimal) {
            Optional<Double> low = decimalLiteral(min);
            Optional<Double> high = decimalLiteral(max);
            if (low.isEmpty() || high.isEmpty()) {
                return false;
            }
            item.put("m_doubleRandomMin", low.get());
            item.put("m_doubleRandomMax", high.get());
        } else {
            if (!(min instanceof Expr.IntLiteral low) || !(max instanceof Expr.IntLiteral high)) {
                compilation.error(call.range(), "random for an int needs two whole numbers: random(1, 10).");
                return false;
            }
            item.put("m_intRandomMin", low.value());
            item.put("m_intRandomMax", high.value());
        }
        return true;
    }

    private Optional<Double> decimalLiteral(Expr expr) {
        return switch (expr) {
            case Expr.DecimalLiteral d -> Optional.of(d.value());
            case Expr.IntLiteral i -> Optional.of((double) i.value());
            default -> {
                compilation.error(expr.range(), "random needs number literals: random(0.0, 1.0).");
                yield Optional.empty();
            }
        };
    }

    /** {@code prompt(title: ..., message: ...)}: asks the user for the value when the macro runs. */
    private boolean prompt(ObjectNode item, Expr.Call call) {
        item.put("m_userPrompt", true);
        return compilation.commands().writeArguments(setVariableSpec(), HelperCalls.rename(call, PROMPT_ARGUMENTS), item, false);
    }

    private boolean expression(ObjectNode item, String text) {
        item.put("m_intExpression", true);
        item.put("m_expression", text);
        return true;
    }

    // ----- small helpers -----------------------------------------------------------------

    /** The magic text of the assignment target, for x += ... forms. */
    private String selfMagicText(Stmt.Assignment assignment) {
        Stmt.Target target = assignment.target();
        Expr reference = new Expr.Name(target.name(), target.nameRange());
        for (Expr key : target.keys()) {
            reference = new Expr.Index(reference, key, target.range());
        }
        return compilation.magicText().reference(reference).orElse("");
    }

    private boolean isSameVariable(Expr expr, Stmt.Target target) {
        return expr instanceof Expr.Name name && name.name().equals(target.name()) && !target.isEntry();
    }

    private static boolean isIntLiteral(Expr expr, long value) {
        return expr instanceof Expr.IntLiteral i && i.value() == value;
    }

    /** The type of a value, or null when it cannot be known before the macro runs. */
    private VarType typeOf(Expr expr) {
        return switch (expr) {
            case Expr.IntLiteral i -> VarType.INT;
            case Expr.DecimalLiteral d -> VarType.DECIMAL;
            case Expr.BoolLiteral b -> VarType.BOOL;
            case Expr.StringLiteral s -> VarType.STRING;
            case Expr.ColorLiteral c -> VarType.COLOR;
            case Expr.Cast c -> c.type();
            case Expr.Name n -> compilation.scope().variable(n.name()).map(VariableSymbol::type).orElse(null);
            case Expr.Binary b when b.operator().isArithmetic() -> {
                VarType left = typeOf(b.left());
                VarType right = typeOf(b.right());
                if (left == VarType.STRING || right == VarType.STRING) {
                    yield VarType.STRING;
                }
                yield left == VarType.DECIMAL || right == VarType.DECIMAL ? VarType.DECIMAL : left != null ? left : right;
            }
            default -> null;
        };
    }

    private boolean typeMismatch(Expr value, VariableSymbol symbol, VarType valueType) {
        compilation.error(value.range(), "'" + symbol.name() + "' is a " + symbol.type().keyword()
                + ", but this value is a " + valueType.keyword() + ".");
        return false;
    }

    private boolean wrongOperator(Stmt.Assignment assignment, String typeName) {
        compilation.error(assignment.range(), "The operator " + assignment.op().symbol() + " cannot be used with "
                + typeName + " variable.");
        return false;
    }

    private CommandSpec setVariableSpec() {
        return compilation.mapping().command(CommandCategory.ACTION, "setVariable").orElseThrow();
    }
}
