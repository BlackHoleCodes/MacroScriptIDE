package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Declaration;
import com.blackholecodes.macroscript.ast.Modifier;
import com.blackholecodes.macroscript.ast.VarType;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** A declared variable. */
public record VariableSymbol(String name, VarType type, Declaration declaration) {

    public boolean isGlobal() {
        return declaration.has(Modifier.GLOBAL);
    }

    public boolean isInput() {
        return declaration.has(Modifier.IN);
    }

    public boolean isOutput() {
        return declaration.has(Modifier.OUT);
    }

    /** The magic text prefix: {@code lv} for local variables, {@code v} for global ones. */
    public String magicTextPrefix() {
        return isGlobal() ? "v" : "lv";
    }

    /** The MacroDroidVariable reference object: {@code {m_name, m_type, isLocalVar}}. */
    public ObjectNode toReference() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("m_name", name);
        node.put("m_type", type.typeCode());
        node.put("isLocalVar", !isGlobal());
        return node;
    }
}
