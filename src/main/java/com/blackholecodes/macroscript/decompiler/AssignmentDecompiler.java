package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.ast.VarType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/** Writes a SetVariableAction as an assignment. This is the reverse of AssignmentCompiler. */
final class AssignmentDecompiler {

    private final Decompilation decompilation;

    AssignmentDecompiler(Decompilation decompilation) {
        this.decompilation = decompilation;
    }

    /** The assignment statement, without the semicolon. */
    String assignment(JsonNode item) {
        String name = decompilation.variables().register(item.path("m_variable"));
        JsonNode keys = item.get("dictionaryKeys");
        boolean entry = keys != null && keys.isArray() && !keys.isEmpty();
        String target = name + (entry ? decompilation.keySuffix(keys) : "");
        VarType type = entry
                ? VarType.fromTypeCode(item.path("dictionaryOrArrayType").asInt(-1))
                        .orElseThrow(() -> new UnrepresentableException("An entry assignment with no type"))
                : VarType.fromTypeCode(item.path("m_variable").path("m_type").asInt(-1))
                        .orElseThrow(() -> new UnrepresentableException("A variable with no type"));

        if (item.path("m_userPrompt").asBoolean()) {
            return target + " = prompt(" + String.join(", ", promptArguments(item)) + ")";
        }
        return switch (type) {
            case BOOL -> target + " = " + boolValue(item, target, entry);
            case INT -> intAssignment(item, target);
            case DECIMAL -> decimalAssignment(item, target);
            case STRING -> target + " = " + decompilation.textLiteral(item.path("m_newStringValue").asText(""));
            case COLOR -> target + " = " + colorValue(item.path("m_newStringValue").asText(""));
            case DICT, ARRAY -> target + " = " + copiedCollection(item);
        };
    }

    private String boolValue(JsonNode item, String target, boolean entry) {
        if (item.path("m_booleanInvert").asBoolean()) {
            if (entry) {
                throw new UnrepresentableException("Inverting a dict entry");
            }
            return "!" + target;
        }
        if (item.hasNonNull("m_otherBooleanVariable")) {
            return decompilation.variables().register(item.path("m_otherBooleanVariable"));
        }
        return Boolean.toString(item.path("m_newBooleanValue").asBoolean(false));
    }

    private String intAssignment(JsonNode item, String target) {
        if (item.path("m_intValueIncrement").asBoolean()) {
            return target + "++";
        }
        if (item.path("m_intValueDecrement").asBoolean()) {
            return target + "--";
        }
        if (item.path("m_intRandom").asBoolean()) {
            return target + " = random(" + item.path("m_intRandomMin").asLong() + ", " + item.path("m_intRandomMax").asLong() + ")";
        }
        if (item.path("m_intExpression").asBoolean()) {
            return target + " = " + decompilation.arithmetic().read(item.path("m_expression").asText(""));
        }
        return target + " = " + item.path("m_newIntValue").asLong(0);
    }

    private String decimalAssignment(JsonNode item, String target) {
        if (item.path("m_intRandom").asBoolean()) {
            return target + " = random(" + JsonSource.decimal(item.path("m_doubleRandomMin").asDouble()) + ", "
                    + JsonSource.decimal(item.path("m_doubleRandomMax").asDouble()) + ")";
        }
        if (item.path("m_intExpression").asBoolean()) {
            return target + " = " + decompilation.arithmetic().read(item.path("m_expression").asText(""));
        }
        return target + " = " + JsonSource.decimal(item.path("m_newDoubleValue").asDouble(0));
    }

    private static String colorValue(String text) {
        if (!text.matches("#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})")) {
            throw new UnrepresentableException("The color " + text + " is not a color literal");
        }
        return text.toUpperCase(java.util.Locale.ROOT);
    }

    private String copiedCollection(JsonNode item) {
        JsonNode location = item.path("copyDictionaryLocation");
        if (!location.path("variableName").isTextual()) {
            throw new UnrepresentableException("A dict assignment with no source");
        }
        return decompilation.variables().registerName(location.path("variableName").asText())
                + decompilation.keySuffix(location.get("dictionaryKeys"));
    }

    private List<String> promptArguments(JsonNode item) {
        List<String> arguments = new ArrayList<>();
        addText(arguments, item, "m_userPromptTitle", "title");
        addText(arguments, item, "m_userPromptMessage", "message");
        addFlag(arguments, item, "m_userPromptPassword", "password", false);
        addFlag(arguments, item, "m_userPromptPasswordToggle", "passwordToggle", false);
        addFlag(arguments, item, "m_userPromptEmptyAtStart", "emptyAtStart", false);
        addFlag(arguments, item, "m_userPromptShowCancel", "showCancel", true);
        addFlag(arguments, item, "m_userPromptStopAfterCancel", "stopAfterCancel", true);
        addText(arguments, item, "m_trueLabel", "trueLabel");
        addText(arguments, item, "m_falseLabel", "falseLabel");
        return arguments;
    }

    private void addText(List<String> arguments, JsonNode item, String field, String name) {
        if (item.path(field).isTextual()) {
            arguments.add(name + ": " + decompilation.textLiteral(item.path(field).asText()));
        }
    }

    private static void addFlag(List<String> arguments, JsonNode item, String field, String name, boolean defaultValue) {
        if (item.path(field).isBoolean() && item.path(field).asBoolean() != defaultValue) {
            arguments.add(name + ": " + item.path(field).asBoolean());
        }
    }
}
