package com.blackholecodes.macroscript.ast;

import java.util.Optional;

/** The seven variable types of MacroScript, with the MacroDroid type code of each. */
public enum VarType {
    BOOL("bool", 0, "BooleanValue", "booleanValue"),
    INT("int", 1, "IntegerValue", "intValue"),
    STRING("string", 2, "StringValue", "textValue"),
    DECIMAL("decimal", 3, "DecimalValue", "decimalValue"),
    DICT("dict", 4, "Dictionary", null),
    ARRAY("array", 5, "Dictionary", null),
    COLOR("color", 6, null, null);

    private final String keyword;
    private final int typeCode;
    private final String entryType;
    private final String entryValueField;

    VarType(String keyword, int typeCode, String entryType, String entryValueField) {
        this.keyword = keyword;
        this.typeCode = typeCode;
        this.entryType = entryType;
        this.entryValueField = entryValueField;
    }

    /** The MacroScript keyword, for example {@code int}. */
    public String keyword() {
        return keyword;
    }

    /** The MacroDroid {@code m_type} code. */
    public int typeCode() {
        return typeCode;
    }

    /** The {@code type} of a dictionary entry that holds a value of this type. */
    public String entryType() {
        return entryType;
    }

    /** The value field of a dictionary entry that holds a value of this type. */
    public String entryValueField() {
        return entryValueField;
    }

    public boolean isCollection() {
        return this == DICT || this == ARRAY;
    }

    /** True for the types a dictionary entry can hold as a plain value. */
    public boolean isScalar() {
        return this == BOOL || this == INT || this == STRING || this == DECIMAL;
    }

    public static Optional<VarType> fromKeyword(String keyword) {
        for (VarType type : values()) {
            if (type.keyword.equals(keyword)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static Optional<VarType> fromTypeCode(int code) {
        for (VarType type : values()) {
            if (type.typeCode == code) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
