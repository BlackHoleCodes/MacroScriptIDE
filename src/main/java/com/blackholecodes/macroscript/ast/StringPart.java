package com.blackholecodes.macroscript.ast;

/** One piece of a string literal: plain text, an interpolation or an escaped brace. */
public sealed interface StringPart {

    /** Plain text, with escapes already resolved. */
    record Text(String text) implements StringPart {
    }

    /** A {@code ${expression}} part. */
    record Interpolation(Expr expression) implements StringPart {
    }

    /** A brace written as {@code \{}. It shows as a literal brace in MacroDroid. */
    record EscapedBrace() implements StringPart {
    }
}
