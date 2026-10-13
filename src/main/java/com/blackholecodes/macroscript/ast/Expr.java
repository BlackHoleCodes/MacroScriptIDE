package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

import java.util.List;
import java.util.Optional;

/** An expression in MacroScript code. */
public sealed interface Expr {

    SourceRange range();

    record IntLiteral(long value, SourceRange range) implements Expr {
    }

    record DecimalLiteral(double value, SourceRange range) implements Expr {
    }

    record BoolLiteral(boolean value, SourceRange range) implements Expr {
    }

    record NullLiteral(SourceRange range) implements Expr {
    }

    /** A string literal. Use {@link #plainText()} when the string has no interpolation. */
    record StringLiteral(List<StringPart> parts, SourceRange range) implements Expr {

        public boolean hasInterpolation() {
            return parts.stream().anyMatch(part -> part instanceof StringPart.Interpolation);
        }

        /** The text of the string, with escaped braces as plain braces and no interpolation. */
        public Optional<String> plainText() {
            StringBuilder text = new StringBuilder();
            for (StringPart part : parts) {
                switch (part) {
                    case StringPart.Text t -> text.append(t.text());
                    case StringPart.EscapedBrace b -> text.append('{');
                    case StringPart.Interpolation i -> {
                        return Optional.empty();
                    }
                }
            }
            return Optional.of(text.toString());
        }
    }

    /** A duration such as {@code 1m30s}, stored in milliseconds. */
    record DurationLiteral(long millis, SourceRange range) implements Expr {
    }

    /** A color such as {@code #FF2196F3}, stored as a signed ARGB int. */
    record ColorLiteral(int argb, String text, SourceRange range) implements Expr {
    }

    record ListLiteral(List<Expr> elements, SourceRange range) implements Expr {
    }

    record ObjectLiteral(List<ObjectEntry> entries, SourceRange range) implements Expr {
    }

    record ObjectEntry(String key, Expr value, SourceRange keyRange) {
    }

    /** A bare name: a variable, a constant, a trigger label or a loop alias. */
    record Name(String name, SourceRange range) implements Expr {
    }

    /** A system value such as {@code sys.battery}. */
    record SysValue(String name, SourceRange range) implements Expr {
    }

    /** A call such as {@code setWifi(TURN_ON)}. */
    record Call(String name, List<Argument> arguments, SourceRange nameRange, SourceRange range) implements Expr {
    }

    /** One argument of a call. The name is null for an argument written without a name. */
    record Argument(String name, Expr value, SourceRange range) {
    }

    /** An index into a dict or array, for example {@code d["key"]}. */
    record Index(Expr target, Expr index, SourceRange range) implements Expr {
    }

    /** A method call such as {@code s.contains("a")}. */
    record MethodCall(Expr target, String method, List<Argument> arguments, SourceRange range) implements Expr {
    }

    record Cast(VarType type, Expr operand, SourceRange range) implements Expr {
    }

    record Not(Expr operand, SourceRange range) implements Expr {
    }

    record Negate(Expr operand, SourceRange range) implements Expr {
    }

    record Binary(Operator operator, Expr left, Expr right, SourceRange range) implements Expr {
    }
}
