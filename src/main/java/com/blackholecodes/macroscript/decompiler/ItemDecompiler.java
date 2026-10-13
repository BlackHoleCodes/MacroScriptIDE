package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.compiler.JsonPaths;
import com.blackholecodes.macroscript.kinds.ParamKinds;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.CommandStatus;
import com.blackholecodes.macroscript.mapping.ParamKind;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Writes one trigger, constraint or action item as a MacroScript call, using the mapping to
 * turn fields back into arguments. An item that the mapping cannot describe exactly is
 * written as {@code raw("ClassType", {...})}, so nothing is lost.
 */
final class ItemDecompiler {

    /** Fields that every item has. The statement writer handles them, not the call. */
    static final Set<String> STATEMENT_FIELDS = Set.of("m_classType", "m_SIGUID", "m_constraintList",
            "m_isOrCondition", "m_isDisabled", "m_comment");
    /** Fields that the call never writes, also when the statement does not handle the rest. */
    private static final Set<String> IDENTITY_FIELDS = Set.of("m_classType", "m_SIGUID");

    private final Decompilation decompilation;

    ItemDecompiler(Decompilation decompilation) {
        this.decompilation = decompilation;
    }

    /**
     * The call for an item that stands inside another value (a nested trigger or a
     * constraint in a condition). Its constraint list and comment are part of the call.
     */
    String call(JsonNode item, CommandCategory category) {
        return call(item, category, false);
    }

    /**
     * The call for an item.
     *
     * @param statementLevel true when the caller writes the constraints, @disabled and
     *                       comment of the item itself
     */
    String call(JsonNode item, CommandCategory category, boolean statementLevel) {
        try {
            return mappedCall(item, category, statementLevel);
        } catch (UnrepresentableException e) {
            decompilation.warn(item.path("m_classType").asText() + " is written as raw(...): " + e.getMessage());
            return raw(item, statementLevel);
        }
    }

    /** The call for an item, or an exception when it cannot be written with the mapping. */
    String mappedCall(JsonNode item, CommandCategory category, boolean statementLevel) {
        String classType = item.path("m_classType").asText();
        CommandSpec spec = decompilation.mapping().commandByClassType(category, classType)
                .orElseThrow(() -> new UnrepresentableException("No command for " + classType));
        if (spec.status() == CommandStatus.SYNTAX) {
            throw new UnrepresentableException(classType + " is written with syntax");
        }
        return commandName(spec) + "(" + String.join(", ", arguments(spec, item, statementLevel)) + ")";
    }

    /** The arguments of a call, the positional one first. Checks that every field is used. */
    List<String> arguments(CommandSpec spec, JsonNode item, boolean statementLevel) {
        Set<String> handled = new HashSet<>(statementLevel ? STATEMENT_FIELDS : IDENTITY_FIELDS);
        Set<String> filledFields = filledFields(spec);
        List<String> arguments = new ArrayList<>();
        boolean positionalWritten = false;
        for (ParamSpec param : spec.params()) {
            if (param.hasOutputField()) {
                handled.add(param.outputPath());
            }
            if (param.dictPathField() != null) {
                handled.add(siblingPath(param, param.dictPathField()));
            }
            if (param.companion() != null) {
                handled.add(siblingPath(param, param.companion()));
            }
            if (param.kind() == ParamKind.FIXED || param.filledBy() != null
                    || (param.hasOutputField() && filledFields.contains(param.jsonField()))) {
                continue;
            }
            ValueContext context = new ValueContext(decompilation, spec, param, withShownDefault(param, item));
            if (param.hasOutputField() && (context.value() == null || context.value().isNull())) {
                continue;
            }
            Optional<String> source = ParamKinds.instance().handler(param.kind()).decompile(context);
            if (source.isEmpty() || (!param.positional() && !param.required() && param.isDefault(context.value()))) {
                continue;
            }
            if (param.positional() && !positionalWritten) {
                arguments.add(0, source.get());
                positionalWritten = true;
            } else {
                arguments.add(argumentName(spec, param) + ": " + source.get());
            }
        }
        checkAllFieldsHandled(item, handled, "");
        return arguments;
    }

    /**
     * The item, with the default value filled in for a positional argument that is absent.
     * The main argument of a command is easier to read when it is shown: airplaneMode(false).
     */
    private static JsonNode withShownDefault(ParamSpec param, JsonNode item) {
        if (!param.positional() || param.defaultValue() == null || !param.hasOutputField()
                || JsonPaths.get(item, param.outputPath()) != null || !(item instanceof ObjectNode object)) {
            return item;
        }
        ObjectNode copy = object.deepCopy();
        JsonPaths.put(copy, param.outputPath(), param.defaultJson());
        return copy;
    }

    /** The JSON fields that the compiler fills from another argument. */
    private static Set<String> filledFields(CommandSpec spec) {
        Set<String> fields = new HashSet<>();
        for (ParamSpec param : spec.params()) {
            if (param.dictPathField() != null) {
                fields.add(param.dictPathField());
            }
            if (param.companion() != null) {
                fields.add(param.companion());
            }
        }
        return fields;
    }

    private static String siblingPath(ParamSpec param, String field) {
        return param.outputPath() == null ? field : JsonPaths.sibling(param.outputPath(), field);
    }

    /** The argument name to write. An argument alias such as {@code title} is preferred. */
    private static String argumentName(CommandSpec spec, ParamSpec param) {
        for (Map.Entry<String, String> alias : spec.paramAliases().entrySet()) {
            if (alias.getValue().equals(param.jsonField())) {
                return alias.getKey();
            }
        }
        return param.name();
    }

    /** The name to call a command by. A short alias such as {@code manual} or {@code notify} is preferred. */
    static String commandName(CommandSpec spec) {
        return spec.aliases().isEmpty() ? spec.name() : spec.aliases().get(0);
    }

    /** Throws when the item has a field with a real value that no argument wrote. */
    private static void checkAllFieldsHandled(JsonNode node, Set<String> handled, String prefix) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String path = prefix + field.getKey();
            if (handled.contains(path) || isEmptyValue(field.getValue())) {
                continue;
            }
            boolean hasHandledChildren = handled.stream().anyMatch(h -> h.startsWith(path + "."));
            if (field.getValue().isObject() && hasHandledChildren) {
                checkAllFieldsHandled(field.getValue(), handled, path + ".");
            } else {
                throw new UnrepresentableException("The field " + path + " has no argument");
            }
        }
    }

    /** Values that carry no information: null, false, 0, "" and empty lists and objects. */
    static boolean isEmptyValue(JsonNode value) {
        return value.isNull()
                || (value.isBoolean() && !value.booleanValue())
                || (value.isNumber() && value.doubleValue() == 0)
                || (value.isTextual() && value.asText().isEmpty())
                || (value.isContainerNode() && value.isEmpty());
    }

    /**
     * {@code raw("ClassType", {fields})}, which the compiler copies as written.
     *
     * @param statementLevel true when the statement writes the constraints and comment
     */
    String raw(JsonNode item, boolean statementLevel) {
        Set<String> skipped = statementLevel ? STATEMENT_FIELDS : IDENTITY_FIELDS;
        ObjectNode fields = JsonNodeFactory.instance.objectNode();
        item.fields().forEachRemaining(field -> {
            if (!skipped.contains(field.getKey())) {
                fields.set(field.getKey(), field.getValue());
            }
        });
        String classType = StringLiterals.quote(item.path("m_classType").asText());
        return fields.isEmpty() ? "raw(" + classType + ")" : "raw(" + classType + ", " + JsonSource.literal(fields) + ")";
    }
}
