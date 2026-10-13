package com.blackholecodes.macroscript.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads {@code macroscript-mapping.json} into a {@link Mapping}.
 *
 * <p>The file is read field by field, not with data binding, so each field the program
 * depends on is visible in this one class.
 */
public final class MappingLoader {

    /** The class path location of the mapping file that ships with the program. */
    public static final String DEFAULT_RESOURCE = "/com/blackholecodes/macroscript/macroscript-mapping.json";

    private static volatile Mapping defaultMapping;

    private MappingLoader() {
    }

    /** Loads the bundled mapping file once and returns the shared copy. */
    public static Mapping loadDefault() {
        Mapping mapping = defaultMapping;
        if (mapping == null) {
            synchronized (MappingLoader.class) {
                mapping = defaultMapping;
                if (mapping == null) {
                    try (InputStream in = MappingLoader.class.getResourceAsStream(DEFAULT_RESOURCE)) {
                        if (in == null) {
                            throw new IllegalStateException("Mapping file not found on class path: " + DEFAULT_RESOURCE);
                        }
                        mapping = load(in);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Could not read the mapping file", e);
                    }
                    defaultMapping = mapping;
                }
            }
        }
        return mapping;
    }

    public static Mapping load(InputStream in) throws IOException {
        JsonNode root = new ObjectMapper().readTree(in);

        Map<CommandCategory, List<CommandSpec>> commands = new EnumMap<>(CommandCategory.class);
        for (CommandCategory category : CommandCategory.values()) {
            List<CommandSpec> list = new ArrayList<>();
            for (JsonNode node : root.path(category.sectionName())) {
                list.add(readCommand(node, category));
            }
            commands.put(category, list);
        }

        List<SystemValue> systemValues = new ArrayList<>();
        for (JsonNode node : root.path("systemValues")) {
            systemValues.add(new SystemValue(node.path("name").asText(),
                    node.path("macroDroid").asText(), node.path("source").asText()));
        }

        Map<String, List<String>> dayGroups = new LinkedHashMap<>();
        root.path("dayGroups").fields().forEachRemaining(entry -> {
            if (entry.getValue().isArray()) {
                dayGroups.put(entry.getKey(), strings(entry.getValue()));
            }
        });

        return new Mapping(
                root.path("macroScriptVersion").asText(),
                root.path("macroDroidVersion").asText(),
                commands,
                systemValues,
                dayGroups,
                strings(root.path("reservedWords")));
    }

    private static CommandSpec readCommand(JsonNode node, CommandCategory category) {
        CommandStatus status = CommandStatus.fromJson(node.path("status").asText("callable"));
        String classType = node.path("classType").asText();
        if (status == CommandStatus.ALIAS && node.hasNonNull("aliasFor")) {
            classType = node.path("aliasFor").asText();
        }

        List<ParamSpec> params = new ArrayList<>();
        for (JsonNode paramNode : node.path("params")) {
            params.add(readParam(paramNode));
        }

        Map<String, String> paramAliases = new LinkedHashMap<>();
        node.path("paramAliases").fields().forEachRemaining(e -> paramAliases.put(e.getKey(), e.getValue().asText()));

        return new CommandSpec(
                node.path("name").asText(),
                classType,
                category,
                status,
                node.path("description").asText(""),
                textOrNull(node, "syntax"),
                textOrNull(node, "sugar"),
                strings(node.path("aliases")),
                paramAliases,
                List.copyOf(params),
                strings(node.path("notes")),
                textOrNull(node, "warning"));
    }

    private static ParamSpec readParam(JsonNode node) {
        List<ConstantSpec> constants = new ArrayList<>();
        for (JsonNode constant : node.path("constants")) {
            constants.add(new ConstantSpec(constant.path("name").asText(), constant.path("value"),
                    constant.path("label").asText("")));
        }
        String unit = textOrNull(node, "unit");
        String emit = textOrNull(node, "emit");
        String range = textOrNull(node, "range");
        return new ParamSpec(
                node.path("name").asText(),
                textOrNull(node, "jsonField"),
                textOrNull(node, "jsonPath"),
                node.path("schemaType").asText(""),
                ParamKind.fromJson(node.path("kind").asText()),
                node.path("required").asBoolean(false),
                node.has("default") ? node.get("default") : null,
                List.copyOf(constants),
                node.path("positional").asBoolean(false),
                node.path("interpolation").asBoolean(false),
                textOrNull(node, "requiredWhen"),
                textOrNull(node, "usedWhen"),
                emit == null ? null : VariableEmit.fromJson(emit),
                textOrNull(node, "filledBy"),
                textOrNull(node, "dictPathField"),
                textOrNull(node, "companion"),
                unit == null ? null : DurationUnit.fromJson(unit),
                range == null ? null : IntRange.parse(range).orElse(null),
                node.path("length").asInt(0),
                textOrNull(node, "order"),
                node.path("description").asText(""));
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static List<String> strings(JsonNode array) {
        List<String> list = new ArrayList<>();
        array.forEach(item -> list.add(item.asText()));
        return List.copyOf(list);
    }
}
