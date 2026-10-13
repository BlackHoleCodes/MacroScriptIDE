package com.blackholecodes.macroscript.ast;

import java.util.Optional;

/** The binary operators of MacroScript expressions. */
public enum Operator {
    MULTIPLY("*", 6),
    DIVIDE("/", 6),
    MODULO("%", 6),
    ADD("+", 5),
    SUBTRACT("-", 5),
    LESS("<", 4),
    GREATER(">", 4),
    LESS_OR_EQUAL("<=", 4),
    GREATER_OR_EQUAL(">=", 4),
    EQUAL("==", 3),
    NOT_EQUAL("!=", 3),
    AND("&&", 2),
    OR("||", 1);

    private final String symbol;
    private final int precedence;

    Operator(String symbol, int precedence) {
        this.symbol = symbol;
        this.precedence = precedence;
    }

    public String symbol() {
        return symbol;
    }

    /** Higher numbers bind more tightly. */
    public int precedence() {
        return precedence;
    }

    public boolean isArithmetic() {
        return precedence >= 5;
    }

    public boolean isComparison() {
        return precedence == 3 || precedence == 4;
    }

    public boolean isLogical() {
        return this == AND || this == OR;
    }

    /** The operator that gives the same result with the operands swapped: 5 < x is x > 5. */
    public Operator mirrored() {
        return switch (this) {
            case LESS -> GREATER;
            case GREATER -> LESS;
            case LESS_OR_EQUAL -> GREATER_OR_EQUAL;
            case GREATER_OR_EQUAL -> LESS_OR_EQUAL;
            default -> this;
        };
    }

    /** The comparison that is true exactly when this one is false: !(a < b) is a >= b. */
    public Operator negated() {
        return switch (this) {
            case LESS -> GREATER_OR_EQUAL;
            case GREATER -> LESS_OR_EQUAL;
            case LESS_OR_EQUAL -> GREATER;
            case GREATER_OR_EQUAL -> LESS;
            case EQUAL -> NOT_EQUAL;
            case NOT_EQUAL -> EQUAL;
            default -> throw new IllegalStateException(this + " is not a comparison");
        };
    }

    public static Optional<Operator> fromSymbol(String symbol) {
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) {
                return Optional.of(op);
            }
        }
        return Optional.empty();
    }
}
