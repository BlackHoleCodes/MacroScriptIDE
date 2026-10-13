package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.Operator;
import com.blackholecodes.macroscript.ast.StringPart;
import com.blackholecodes.macroscript.ast.VarType;

import java.util.List;
import java.util.Optional;

/**
 * Writes MacroDroid magic text: the {@code {lv=name}} and {@code {battery}} codes that
 * MacroDroid fills in when the macro runs.
 *
 * <p>Every method reports its own errors and returns empty after an error.
 */
public final class MagicText {

    private final Compilation compilation;

    MagicText(Compilation compilation) {
        this.compilation = compilation;
    }

    // ----- references --------------------------------------------------------------------

    /** The magic text of a reference, for example {@code {lv=scores[lives]}}. */
    public Optional<String> reference(Reference reference) {
        return switch (reference) {
            case Reference.System s -> Optional.of(s.value().magicText());
            case Reference.LoopKey k -> Optional.of(k.scope().keyMagicText());
            case Reference.LoopValue v -> Optional.of(LoopScope.valueMagicText());
            case Reference.Variable v -> keyPath(v.keys())
                    .map(path -> "{" + v.symbol().magicTextPrefix() + "=" + v.symbol().name() + path + "}");
        };
    }

    /** Resolves an expression as a reference and returns its magic text. */
    public Optional<String> reference(Expr expr) {
        return compilation.references().resolve(expr).flatMap(this::reference);
    }

    /** The {@code [a][b]} part of an entry reference. */
    private Optional<String> keyPath(List<Expr> keys) {
        StringBuilder path = new StringBuilder();
        for (Expr key : keys) {
            Optional<String> text = key(key);
            if (text.isEmpty()) {
                return Optional.empty();
            }
            path.append('[').append(text.get()).append(']');
        }
        return Optional.of(path.toString());
    }

    /**
     * The text of one dict or array key: a string or int literal, or the magic text of a
     * variable that holds the key.
     */
    public Optional<String> key(Expr key) {
        return switch (key) {
            case Expr.StringLiteral s when !s.hasInterpolation() -> s.plainText();
            case Expr.IntLiteral i -> Optional.of(Long.toString(i.value()));
            case Expr.Name n -> reference(n);
            case Expr.Index i -> reference(i);
            case Expr.SysValue s -> reference(s);
            default -> {
                compilation.error(key.range(), "A key must be a string, an int or a variable.");
                yield Optional.empty();
            }
        };
    }

    // ----- strings -----------------------------------------------------------------------

    /** The magic text for a string literal with its {@code ${...}} parts filled in. */
    public Optional<String> text(Expr.StringLiteral literal) {
        StringBuilder out = new StringBuilder();
        boolean ok = true;
        List<StringPart> parts = literal.parts();
        for (int i = 0; i < parts.size(); i++) {
            switch (parts.get(i)) {
                case StringPart.Text t -> out.append(t.text());
                case StringPart.Interpolation in -> {
                    Optional<String> value = reference(in.expression());
                    ok &= value.isPresent();
                    value.ifPresent(out::append);
                }
                case StringPart.EscapedBrace b -> out.append(escapedBrace(parts, i));
            }
        }
        return ok ? Optional.of(out.toString()) : Optional.empty();
    }

    /**
     * MacroDroid shows a backslash that is not part of an escape. So {@code \{} is written only
     * when the text up to the next closing brace would otherwise be read as magic text.
     */
    private String escapedBrace(List<StringPart> parts, int index) {
        if (index + 1 < parts.size() && parts.get(index + 1) instanceof StringPart.Text next) {
            int close = next.text().indexOf('}');
            if (close >= 0 && isMagicTextName(next.text().substring(0, close))) {
                return "\\{";
            }
        }
        return "{";
    }

    /** True when {@code {name}} is magic text that MacroDroid would fill in. */
    public boolean isMagicTextName(String name) {
        return name.startsWith("v=") || name.startsWith("lv=")
                || compilation.mapping().systemValueByMagicText("{" + name + "}").isPresent();
    }

    /**
     * The magic text for a value that goes into a text field: a string literal, a bare
     * variable or sys value, or a number.
     */
    public Optional<String> textValue(Expr value) {
        return switch (value) {
            case Expr.StringLiteral s -> text(s);
            case Expr.IntLiteral i -> Optional.of(Long.toString(i.value()));
            case Expr.DecimalLiteral d -> Optional.of(formatDecimal(d.value()));
            case Expr.BoolLiteral b -> Optional.of(Boolean.toString(b.value()));
            case Expr.Name n -> reference(n);
            case Expr.SysValue s -> reference(s);
            case Expr.Index i -> reference(i);
            case Expr.Cast c -> textValue(c.operand());
            case Expr.Binary b when b.operator() == Operator.ADD -> concatenation(b);
            default -> {
                compilation.error(value.range(), "Expected text here, for example \"Hello ${name}\".");
                yield Optional.empty();
            }
        };
    }

    /** The magic text for {@code "a" + name + "b"}: the parts joined with no separator. */
    public Optional<String> concatenation(Expr expr) {
        if (expr instanceof Expr.Binary binary && binary.operator() == Operator.ADD) {
            Optional<String> left = concatenation(binary.left());
            Optional<String> right = concatenation(binary.right());
            return left.isPresent() && right.isPresent() ? Optional.of(left.get() + right.get()) : Optional.empty();
        }
        return textValue(expr);
    }

    // ----- arithmetic --------------------------------------------------------------------

    /**
     * The text of an int or decimal expression for an {@code m_expression} field, for
     * example {@code {lv=j}*2+1}.
     */
    public Optional<String> arithmetic(Expr expr) {
        return arithmetic(expr, 0);
    }

    private Optional<String> arithmetic(Expr expr, int parentPrecedence) {
        return switch (expr) {
            case Expr.IntLiteral i -> Optional.of(Long.toString(i.value()));
            case Expr.DecimalLiteral d -> Optional.of(formatDecimal(d.value()));
            case Expr.Negate n -> arithmetic(n.operand(), Operator.MULTIPLY.precedence() + 1).map(text -> "-" + text);
            case Expr.Name n -> numericReference(n);
            case Expr.SysValue s -> reference(s);
            case Expr.Index i -> numericReference(i);
            case Expr.Cast c -> arithmetic(c.operand(), parentPrecedence);
            case Expr.Binary b when b.operator().isArithmetic() -> {
                int precedence = b.operator().precedence();
                Optional<String> left = arithmetic(b.left(), precedence);
                // The right side needs parentheses at equal precedence: a - (b - c).
                Optional<String> right = arithmetic(b.right(), precedence + 1);
                if (left.isEmpty() || right.isEmpty()) {
                    yield Optional.empty();
                }
                String text = left.get() + b.operator().symbol() + right.get();
                yield Optional.of(precedence < parentPrecedence ? "(" + text + ")" : text);
            }
            default -> {
                compilation.error(expr.range(), "Expected a number, a number variable or arithmetic (+ - * / %) here.");
                yield Optional.empty();
            }
        };
    }

    private Optional<String> numericReference(Expr expr) {
        Optional<Reference> reference = compilation.references().resolve(expr);
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        VarType type = reference.get().knownType();
        if (type != null && type != VarType.INT && type != VarType.DECIMAL) {
            compilation.error(expr.range(), "'" + describe(expr) + "' is a " + type.keyword()
                    + ", so it cannot be used in arithmetic.");
            return Optional.empty();
        }
        return reference(reference.get());
    }

    private static String describe(Expr expr) {
        return expr instanceof Expr.Name n ? n.name() : "this value";
    }

    /** Writes a decimal the way Java does, for example 1.5 or 2.0. */
    public static String formatDecimal(double value) {
        return Double.toString(value);
    }
}
