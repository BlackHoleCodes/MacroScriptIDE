package com.blackholecodes.macroscript.ast;

import java.util.Optional;

/** The assignment operators. */
public enum AssignOp {
    ASSIGN("=", null),
    ADD("+=", Operator.ADD),
    SUBTRACT("-=", Operator.SUBTRACT),
    MULTIPLY("*=", Operator.MULTIPLY),
    DIVIDE("/=", Operator.DIVIDE),
    MODULO("%=", Operator.MODULO),
    INCREMENT("++", Operator.ADD),
    DECREMENT("--", Operator.SUBTRACT);

    private final String symbol;
    private final Operator arithmetic;

    AssignOp(String symbol, Operator arithmetic) {
        this.symbol = symbol;
        this.arithmetic = arithmetic;
    }

    public String symbol() {
        return symbol;
    }

    /** The operator that a compound assignment applies, or null for plain {@code =}. */
    public Operator arithmetic() {
        return arithmetic;
    }

    public static Optional<AssignOp> fromSymbol(String symbol) {
        for (AssignOp op : values()) {
            if (op.symbol.equals(symbol)) {
                return Optional.of(op);
            }
        }
        return Optional.empty();
    }
}
