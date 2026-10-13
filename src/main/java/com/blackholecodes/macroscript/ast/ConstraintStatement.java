package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

/** One statement of the constraints block. */
public record ConstraintStatement(Expr condition, ItemMeta meta, SourceRange range) {
}
