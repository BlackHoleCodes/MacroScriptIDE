package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.ItemMeta;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Creates MacroDroid items and sets the fields that every item has. */
public final class Items {

    private Items() {
    }

    /** A new item with {@code m_classType} and {@code m_SIGUID} 0. */
    public static ObjectNode create(String classType) {
        ObjectNode item = JsonNodeFactory.instance.objectNode();
        item.put("m_classType", classType);
        item.put("m_SIGUID", 0L);
        return item;
    }

    /** Adds a condition to an item as {@code m_constraintList} and {@code m_isOrCondition}. */
    public static void setConstraints(ObjectNode item, ConstraintList constraints) {
        if (constraints == null || constraints.items().isEmpty()) {
            return;
        }
        item.set("m_constraintList", constraints.toJson());
        if (constraints.isOr()) {
            item.put("m_isOrCondition", true);
        }
    }

    /** Writes {@code @disabled} and the comment of a statement. */
    public static void applyMeta(ObjectNode item, ItemMeta meta) {
        if (meta.disabled()) {
            item.put("m_isDisabled", true);
        }
        if (meta.comment() != null) {
            item.put("m_comment", meta.comment());
        }
    }
}
