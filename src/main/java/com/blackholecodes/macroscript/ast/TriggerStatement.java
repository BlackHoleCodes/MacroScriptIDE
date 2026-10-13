package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

/**
 * One statement of the triggers block.
 *
 * @param label      the name after {@code as}, or null
 * @param when       the condition after {@code when}, or null
 */
public record TriggerStatement(Expr.Call call, String label, SourceRange labelRange, Expr when,
                               ItemMeta meta, SourceRange range) {
}
