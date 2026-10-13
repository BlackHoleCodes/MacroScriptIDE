package com.blackholecodes.macroscript.mapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/**
 * One parameter of a command, as described by the mapping file.
 *
 * @param name           the MacroScript argument name
 * @param jsonField      the field name in the output item
 * @param jsonPath       a dotted path when the field is nested (for example
 *                       {@code requestConfig.urlToOpen}), otherwise null
 * @param schemaType     the MacroDroid schema type, for example {@code dictkeys}
 * @param kind           what the argument accepts in MacroScript code
 * @param required       true when the argument must be given
 * @param defaultValue   the value MacroDroid uses when the field is absent, or null
 * @param constants      the named values of an enum or flags parameter
 * @param positional     true when the argument can be given without its name
 * @param interpolation  true when the value supports magic text
 * @param requiredWhen   a condition on other parameters that makes this one required, or null
 * @param usedWhen       a condition on other parameters under which this one is used, or null
 * @param emit           the JSON form of a variable argument, or null
 * @param filledBy       the parameter whose value also fills this one, or null
 * @param dictPathField  the companion field that holds the dictionary path of a variable, or null
 * @param companion      the companion field that holds the display name of an app, or null
 * @param unit           the unit of a duration parameter, or null
 * @param range          the permitted range of an int parameter, or null
 * @param length         the length of a flags array, or 0 when not given
 * @param order          the day order of a day flags parameter, or null
 * @param description    the documentation text
 */
public record ParamSpec(
        String name,
        String jsonField,
        String jsonPath,
        String schemaType,
        ParamKind kind,
        boolean required,
        JsonNode defaultValue,
        List<ConstantSpec> constants,
        boolean positional,
        boolean interpolation,
        String requiredWhen,
        String usedWhen,
        VariableEmit emit,
        String filledBy,
        String dictPathField,
        String companion,
        DurationUnit unit,
        IntRange range,
        int length,
        String order,
        String description) {

    /** The dotted path of the output field. It is the JSON field when no path is given. */
    public String outputPath() {
        return jsonPath != null ? jsonPath : jsonField;
    }

    /** True when the parameter has an output field of its own. */
    public boolean hasOutputField() {
        return jsonField != null;
    }

    /** True when the compiler fills this parameter from another argument. */
    public boolean isFilledByCompiler() {
        return filledBy != null || kind == ParamKind.DICT_PATH || kind == ParamKind.FIXED;
    }

    /** True when the dictionary path field takes the {@code {"keys": [...]}} object form. */
    public boolean usesKeysObject() {
        return "dictkeys".equals(schemaType);
    }

    public Optional<ConstantSpec> constant(String constantName) {
        return constants.stream().filter(c -> c.name().equals(constantName)).findFirst();
    }

    public Optional<ConstantSpec> constantWithValue(JsonNode value) {
        return constants.stream().filter(c -> c.value().equals(value)).findFirst();
    }

    /**
     * The default value as JSON, or null when there is none. An enum default that the
     * mapping gives as a constant name becomes the constant's value.
     */
    public JsonNode defaultJson() {
        if (defaultValue != null && defaultValue.isTextual()) {
            Optional<ConstantSpec> constant = constant(defaultValue.asText());
            if (constant.isPresent()) {
                return constant.get().value();
            }
        }
        return defaultValue;
    }

    /** True when the value is the default, so it can be left out. Numbers compare by value: 1 equals 1.0. */
    public boolean isDefault(JsonNode value) {
        JsonNode defaultJson = defaultJson();
        if (value == null || defaultJson == null) {
            return false;
        }
        if (value.isNumber() && defaultJson.isNumber()) {
            return value.doubleValue() == defaultJson.doubleValue();
        }
        return value.equals(defaultJson);
    }

    /** True when the parameter appears in the documented signature. */
    public boolean isWrittenByUser() {
        return !isFilledByCompiler() && kind != ParamKind.APP_NAME;
    }
}
