package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

import java.util.List;

/**
 * A top-level block of a file.
 *
 * @param range the range of the block keyword, used for messages about the block
 */
public record Block<T>(List<T> items, SourceRange range) {
}
