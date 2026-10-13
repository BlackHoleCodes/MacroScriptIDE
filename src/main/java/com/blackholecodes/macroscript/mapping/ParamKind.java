package com.blackholecodes.macroscript.mapping;

import java.util.Locale;

/**
 * What a parameter accepts in MacroScript code. Each kind has one handler in the
 * {@code kinds} package that converts between MacroScript values and JSON.
 */
public enum ParamKind {
    BOOL,
    INT,
    DECIMAL,
    STRING,
    TEXT,
    ENUM,
    FLAGS,
    DURATION,
    COLOR,
    COLOR_LIST,
    VARIABLE,
    VARIABLE_LIST,
    DICT_PATH,
    APP,
    APP_LIST,
    APP_NAME,
    CONTACT,
    CONTACT_LIST,
    LIST,
    OBJECT,
    OBJECT_LIST,
    RAW_OBJECT,
    HTTP_PARAMS,
    BLOCK_REF,
    BLOCK_REF_LIST,
    TRIGGER_LIST,
    CONDITION,
    STREAM_MAP,
    FIXED;

    /** Converts a mapping file name such as {@code "colorList"} to {@link #COLOR_LIST}. */
    static ParamKind fromJson(String value) {
        String upperSnake = value.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
        return valueOf(upperSnake);
    }
}
