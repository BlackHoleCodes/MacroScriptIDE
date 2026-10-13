package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.compiler.Compilation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

/** Converts MacroScript literals to JSON values. */
public final class JsonValues {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private JsonValues() {
    }

    /** An int node when the value fits in an int, otherwise a long node. */
    public static JsonNode number(long value) {
        return value == (int) value ? JSON.numberNode((int) value) : JSON.numberNode(value);
    }

    /**
     * Converts a literal, list or object to JSON with no knowledge of the target field.
     *
     * @param magicText true when strings and bare variable names become magic text
     */
    public static Optional<JsonNode> convert(Compilation compilation, Expr value, boolean magicText) {
        return switch (value) {
            case Expr.IntLiteral i -> Optional.of(number(i.value()));
            case Expr.DecimalLiteral d -> Optional.of(JSON.numberNode(d.value()));
            case Expr.BoolLiteral b -> Optional.of(JSON.booleanNode(b.value()));
            case Expr.NullLiteral n -> Optional.of(JSON.nullNode());
            case Expr.ColorLiteral c -> Optional.of(JSON.numberNode(c.argb()));
            case Expr.StringLiteral s -> string(compilation, s, magicText).map(JSON::textNode);
            case Expr.ListLiteral list -> {
                ArrayNode array = JSON.arrayNode();
                for (Expr element : list.elements()) {
                    Optional<JsonNode> converted = convert(compilation, element, magicText);
                    if (converted.isEmpty()) {
                        yield Optional.empty();
                    }
                    array.add(converted.get());
                }
                yield Optional.of(array);
            }
            case Expr.ObjectLiteral object -> {
                ObjectNode node = JSON.objectNode();
                for (Expr.ObjectEntry entry : object.entries()) {
                    Optional<JsonNode> converted = convert(compilation, entry.value(), magicText);
                    if (converted.isEmpty()) {
                        yield Optional.empty();
                    }
                    node.set(entry.key(), converted.get());
                }
                yield Optional.of(node);
            }
            case Expr.Name n when magicText -> compilation.magicText().reference(n).map(JSON::textNode);
            case Expr.SysValue s when magicText -> compilation.magicText().reference(s).map(JSON::textNode);
            case Expr.Index i when magicText -> compilation.magicText().reference(i).map(JSON::textNode);
            default -> {
                compilation.error(value.range(), "Expected a literal value here (a number, string, bool, list or object).");
                yield Optional.empty();
            }
        };
    }

    /** The text of a string literal, as magic text or as plain text with no ${...} parts. */
    public static Optional<String> string(Compilation compilation, Expr.StringLiteral literal, boolean magicText) {
        if (magicText) {
            return compilation.magicText().text(literal);
        }
        if (literal.hasInterpolation()) {
            compilation.error(literal.range(), "This value does not support ${...}. Write plain text.");
            return Optional.empty();
        }
        return literal.plainText();
    }
}
