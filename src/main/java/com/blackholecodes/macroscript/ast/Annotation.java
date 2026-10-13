package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

/**
 * An annotation such as {@code @name("My Macro")}.
 *
 * @param name     the name without the at sign
 * @param argument the value in parentheses, or null
 */
public record Annotation(String name, Expr argument, SourceRange range) {
}
