package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.ast.VarType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The variables of the macro being decompiled. Local variables come from
 * {@code localVariables}. Global variables are found where the macro uses them, because
 * MacroDroid exports do not list them.
 */
final class VariableRegistry {

    private final Map<String, VarType> locals = new LinkedHashMap<>();
    private final Map<String, VarType> globals = new LinkedHashMap<>();
    private final java.util.Set<String> guessedGlobalTypes = new java.util.HashSet<>();

    void addLocal(String name, VarType type) {
        locals.put(name, type);
    }

    boolean isLocal(String name) {
        return locals.containsKey(name);
    }

    /** Records a global variable. A known type replaces a guessed one. */
    void addGlobal(String name, VarType type, boolean typeKnown) {
        if (typeKnown) {
            globals.put(name, type);
            guessedGlobalTypes.remove(name);
        } else if (!globals.containsKey(name)) {
            globals.put(name, type);
            guessedGlobalTypes.add(name);
        }
    }

    /** True when the export did not say the type of a global variable, so it was guessed. */
    boolean isTypeGuessed(String name) {
        return guessedGlobalTypes.contains(name);
    }

    Optional<VarType> type(String name) {
        VarType type = locals.get(name);
        return Optional.ofNullable(type != null ? type : globals.get(name));
    }

    boolean isDeclared(String name) {
        return locals.containsKey(name) || globals.containsKey(name);
    }

    Map<String, VarType> globals() {
        return globals;
    }

    /**
     * Records the variable of a MacroDroidVariable object ({@code {m_name, m_type, isLocalVar}})
     * and returns its name.
     */
    String register(JsonNode reference) {
        String name = reference.path("m_name").asText();
        boolean local = reference.path("isLocalVar").asBoolean(true);
        Optional<VarType> type = VarType.fromTypeCode(reference.path("m_type").asInt(-1));
        if (!local || !isLocal(name)) {
            if (!local) {
                addGlobal(name, type.orElse(VarType.STRING), type.isPresent());
            } else if (!isDeclared(name)) {
                throw new UnrepresentableException("The local variable '" + name + "' is not in localVariables.");
            }
        }
        return name;
    }

    /** Records a variable known only by name, for example in {@code variableName} fields. */
    String registerName(String name) {
        if (!isDeclared(name)) {
            addGlobal(name, VarType.STRING, false);
        }
        return name;
    }
}
