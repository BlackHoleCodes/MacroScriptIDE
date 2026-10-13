package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.mapping.SystemValue;

import java.util.List;

/**
 * Something an expression can read a value from: a variable (optionally a dict or array
 * entry), a system value, or the key or value of the innermost for loop.
 */
public sealed interface Reference {

    /** A variable, or an entry of a dict or array variable when {@code keys} is not empty. */
    record Variable(VariableSymbol symbol, List<Expr> keys) implements Reference {

        public boolean isEntry() {
            return !keys.isEmpty();
        }
    }

    record System(SystemValue value) implements Reference {
    }

    /** The key alias of a for loop. */
    record LoopKey(LoopScope scope) implements Reference {
    }

    /** The value alias of a for loop. */
    record LoopValue(LoopScope scope) implements Reference {
    }

    /** The type of the value, or null when the compiler cannot know it. */
    default VarType knownType() {
        return switch (this) {
            case Variable v -> v.isEntry() ? null : v.symbol().type();
            case LoopKey k -> k.scope().isArray() ? VarType.INT : VarType.STRING;
            case System s -> null;
            case LoopValue l -> null;
        };
    }
}
