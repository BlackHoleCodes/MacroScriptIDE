package com.blackholecodes.macroscript.compiler;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * A compiled condition: the constraint items and whether they are joined with OR.
 *
 * @param isOr true when any one item is enough (m_isOrCondition), false when all must be true
 */
public record ConstraintList(List<ObjectNode> items, boolean isOr) {

    public static final ConstraintList EMPTY = new ConstraintList(List.of(), false);

    public ArrayNode toJson() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        items.forEach(array::add);
        return array;
    }
}
