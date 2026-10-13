package com.blackholecodes.macroscript.ast;

/**
 * Settings that a statement annotation or a doc comment puts on one MacroDroid item.
 *
 * @param disabled true when the statement has {@code @disabled}
 * @param comment  the text of {@code @comment(...)} or a doc comment, or null
 */
public record ItemMeta(boolean disabled, String comment) {

    public static final ItemMeta NONE = new ItemMeta(false, null);
}
