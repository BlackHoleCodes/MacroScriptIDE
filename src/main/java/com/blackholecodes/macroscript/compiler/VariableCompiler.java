package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Declaration;
import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.Modifier;
import com.blackholecodes.macroscript.ast.VarType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

/**
 * Compiles the variables block. Each local variable becomes one entry of
 * {@code macro.localVariables}. Global variables are only declared: they must already
 * exist on the phone, so they are never written to the output.
 */
final class VariableCompiler {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final Compilation compilation;

    VariableCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    /** Declares the variables and returns the localVariables array. */
    ArrayNode compile(List<Declaration> declarations, boolean actionBlock) {
        ArrayNode localVariables = JSON.arrayNode();
        for (Declaration declaration : declarations) {
            if (!checkModifiers(declaration, actionBlock)) {
                continue;
            }
            VariableSymbol symbol = new VariableSymbol(declaration.name(), declaration.type(), declaration);
            if (!compilation.scope().declare(symbol)) {
                compilation.error(declaration.nameRange(), "The variable '" + declaration.name() + "' is declared twice.");
                continue;
            }
            if (!symbol.isGlobal()) {
                localVariable(declaration).ifPresent(localVariables::add);
            }
        }
        return localVariables;
    }

    private boolean checkModifiers(Declaration declaration, boolean actionBlock) {
        if (declaration.has(Modifier.GLOBAL)) {
            if (declaration.initializer() != null) {
                compilation.error(declaration.initializer().range(), "A global variable cannot have an initial value. "
                        + "Its value is on the phone, and an import must not change it.");
                return false;
            }
            if (declaration.has(Modifier.IN) || declaration.has(Modifier.OUT) || declaration.has(Modifier.SECURE)) {
                compilation.error(declaration.range(), "A global variable cannot also be in, out or secure.");
                return false;
            }
        }
        if (!actionBlock && (declaration.has(Modifier.IN) || declaration.has(Modifier.OUT))) {
            compilation.error(declaration.range(), "in and out are for action blocks. Add @actionBlock to the file, "
                    + "or remove in and out.");
            return false;
        }
        if (declaration.type() == VarType.COLOR && declaration.initializer() != null) {
            compilation.error(declaration.initializer().range(), "A color variable cannot have an initial value. "
                    + "Write color " + declaration.name() + "; and assign it in the actions block.");
            return false;
        }
        return true;
    }

    private Optional<ObjectNode> localVariable(Declaration declaration) {
        ObjectNode variable = JSON.objectNode();
        variable.put("m_name", declaration.name());
        variable.put("m_type", declaration.type().typeCode());
        Expr initializer = declaration.initializer();
        boolean ok = switch (declaration.type()) {
            case BOOL -> writeBool(variable, initializer);
            case INT -> writeInt(variable, initializer);
            case STRING -> writeString(variable, initializer);
            case DECIMAL -> writeDecimal(variable, initializer);
            case DICT -> writeDictionary(variable, initializer, false);
            case ARRAY -> writeDictionary(variable, initializer, true);
            case COLOR -> true;
        };
        variable.put("isLocalVar", true);
        if (declaration.has(Modifier.SECURE)) {
            variable.put("isSecure", true);
        }
        if (declaration.has(Modifier.IN)) {
            variable.put("supportsInput", true);
        }
        if (declaration.has(Modifier.OUT)) {
            variable.put("supportsOutput", true);
        }
        return ok ? Optional.of(variable) : Optional.empty();
    }

    private boolean writeBool(ObjectNode variable, Expr initializer) {
        if (initializer == null || initializer instanceof Expr.BoolLiteral) {
            variable.put("m_booleanValue", initializer != null && ((Expr.BoolLiteral) initializer).value());
            return true;
        }
        return wrongInitializer(initializer, "true or false");
    }

    private boolean writeInt(ObjectNode variable, Expr initializer) {
        if (initializer == null || initializer instanceof Expr.IntLiteral) {
            variable.put("m_intValue", initializer == null ? 0 : ((Expr.IntLiteral) initializer).value());
            return true;
        }
        return wrongInitializer(initializer, "a whole number");
    }

    private boolean writeDecimal(ObjectNode variable, Expr initializer) {
        switch (initializer) {
            case null -> variable.put("m_decimalValue", 0.0);
            case Expr.DecimalLiteral d -> variable.put("m_decimalValue", d.value());
            case Expr.IntLiteral i -> variable.put("m_decimalValue", (double) i.value());
            default -> {
                return wrongInitializer(initializer, "a number");
            }
        }
        return true;
    }

    private boolean writeString(ObjectNode variable, Expr initializer) {
        if (initializer == null) {
            variable.put("m_stringValue", "");
            return true;
        }
        if (initializer instanceof Expr.StringLiteral literal) {
            if (literal.hasInterpolation()) {
                compilation.error(initializer.range(), "An initial value cannot use ${...}. Assign the text in the actions block.");
                return false;
            }
            variable.put("m_stringValue", literal.plainText().orElseThrow());
            return true;
        }
        return wrongInitializer(initializer, "a string");
    }

    private boolean writeDictionary(ObjectNode variable, Expr initializer, boolean isArray) {
        if (initializer != null && !(isArray ? initializer instanceof Expr.ListLiteral : initializer instanceof Expr.ObjectLiteral)) {
            return wrongInitializer(initializer, isArray ? "an array literal [a, b]" : "a dict literal {\"key\": value}");
        }
        Optional<ObjectNode> dictionary = initializer == null ? Optional.of(dictionary(JSON.arrayNode(), isArray))
                : dictionaryLiteral(initializer);
        dictionary.ifPresent(d -> variable.set("dictionary", d));
        return dictionary.isPresent();
    }

    /** A dict or array literal as {@code {"entries": [...], "isArray": ...}}. */
    private Optional<ObjectNode> dictionaryLiteral(Expr literal) {
        ArrayNode entries = JSON.arrayNode();
        if (literal instanceof Expr.ListLiteral list) {
            for (int i = 0; i < list.elements().size(); i++) {
                Optional<ObjectNode> entry = entry(Integer.toString(i), list.elements().get(i));
                if (entry.isEmpty()) {
                    return Optional.empty();
                }
                entries.add(entry.get());
            }
            return Optional.of(dictionary(entries, true));
        }
        for (Expr.ObjectEntry objectEntry : ((Expr.ObjectLiteral) literal).entries()) {
            Optional<ObjectNode> entry = entry(objectEntry.key(), objectEntry.value());
            if (entry.isEmpty()) {
                return Optional.empty();
            }
            entries.add(entry.get());
        }
        return Optional.of(dictionary(entries, false));
    }

    private Optional<ObjectNode> entry(String key, Expr value) {
        ObjectNode valueNode = JSON.objectNode();
        switch (value) {
            case Expr.StringLiteral s when !s.hasInterpolation() -> scalar(valueNode, VarType.STRING).put(
                    VarType.STRING.entryValueField(), s.plainText().orElseThrow());
            case Expr.IntLiteral i -> scalar(valueNode, VarType.INT).put(VarType.INT.entryValueField(), i.value());
            case Expr.DecimalLiteral d -> scalar(valueNode, VarType.DECIMAL).put(VarType.DECIMAL.entryValueField(), d.value());
            case Expr.BoolLiteral b -> scalar(valueNode, VarType.BOOL).put(VarType.BOOL.entryValueField(), b.value());
            case Expr.ListLiteral l -> {
                Optional<ObjectNode> nested = dictionaryLiteral(l);
                if (nested.isEmpty()) {
                    return Optional.empty();
                }
                valueNode.put("type", VarType.ARRAY.entryType());
                valueNode.setAll(nested.get());
            }
            case Expr.ObjectLiteral o -> {
                Optional<ObjectNode> nested = dictionaryLiteral(o);
                if (nested.isEmpty()) {
                    return Optional.empty();
                }
                valueNode.put("type", VarType.DICT.entryType());
                valueNode.setAll(nested.get());
            }
            default -> {
                compilation.error(value.range(), "A dict or array entry must be a string, number, bool, dict or array literal.");
                return Optional.empty();
            }
        }
        ObjectNode entry = JSON.objectNode();
        entry.put("key", key);
        entry.set("variable", valueNode);
        return Optional.of(entry);
    }

    private static ObjectNode scalar(ObjectNode valueNode, VarType type) {
        valueNode.put("type", type.entryType());
        return valueNode;
    }

    private static ObjectNode dictionary(ArrayNode entries, boolean isArray) {
        ObjectNode dictionary = JSON.objectNode();
        dictionary.set("entries", entries);
        dictionary.put("isArray", isArray);
        return dictionary;
    }

    private boolean wrongInitializer(Expr initializer, String expected) {
        compilation.error(initializer.range(), "The initial value must be " + expected + ".");
        return false;
    }
}
