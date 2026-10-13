package com.blackholecodes.macroscript.mapping;

/** The JSON form that a variable argument takes in the output. */
public enum VariableEmit {
    /** The plain variable name as a string. */
    VARIABLE_NAME,
    /** A MacroDroidVariable object: {@code {m_name, m_type, isLocalVar}}. */
    VARIABLE_OBJECT,
    /** A VariableWithDictionaryKeys object: {@code {variableName, dictionaryKeys}}. */
    VARIABLE_WITH_KEYS;

    static VariableEmit fromJson(String value) {
        return switch (value) {
            case "variableName" -> VARIABLE_NAME;
            case "variableObject" -> VARIABLE_OBJECT;
            case "variableWithKeys" -> VARIABLE_WITH_KEYS;
            default -> throw new IllegalArgumentException("Unknown emit form: " + value);
        };
    }
}
