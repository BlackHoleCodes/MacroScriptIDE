package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

import java.util.Set;

/**
 * One declared variable. {@code int a, b;} gives two declarations.
 *
 * @param initializer the initial value, or null
 */
public record Declaration(Set<Modifier> modifiers, VarType type, String name, Expr initializer,
                          SourceRange nameRange, SourceRange range) {

    public boolean has(Modifier modifier) {
        return modifiers.contains(modifier);
    }
}
