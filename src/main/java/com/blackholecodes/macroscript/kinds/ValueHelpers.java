package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads the small value helpers {@code app(...)} and {@code contact(...)}. */
final class ValueHelpers {

    private ValueHelpers() {
    }

    /**
     * Reads a helper call whose arguments are 1 to {@code maxArguments} plain strings.
     *
     * @return the strings, or empty after an error was reported
     */
    static Optional<List<String>> strings(ArgumentContext argument, Expr.Call call, int maxArguments, String example) {
        int count = call.arguments().size();
        if (count < 1 || count > maxArguments) {
            argument.error(call, "needs " + example + ".");
            return Optional.empty();
        }
        List<String> values = new ArrayList<>();
        for (Expr.Argument arg : call.arguments()) {
            if (arg.name() != null || !(arg.value() instanceof Expr.StringLiteral literal) || literal.hasInterpolation()) {
                argument.error(arg.value(), "needs " + example + ".");
                return Optional.empty();
            }
            values.add(literal.plainText().orElseThrow());
        }
        return Optional.of(values);
    }

    /** The elements of a list literal, or the value itself as a list of one. */
    static List<Expr> elements(Expr value) {
        return value instanceof Expr.ListLiteral list ? list.elements() : List.of(value);
    }
}
