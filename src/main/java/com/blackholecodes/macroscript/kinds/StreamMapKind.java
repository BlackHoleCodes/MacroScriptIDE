package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Volume levels per audio stream: {@code {MUSIC: 0, RINGER: 50}}. The compiler fills two
 * parallel arrays with one entry per stream: which streams to change, and their levels.
 */
final class StreamMapKind implements ParamKindHandler {

    /** The parameter that says which streams change. */
    static final String INDEX_PARAM = "streamIndexArray";
    /** The parameter that holds the volume percent of each stream. */
    static final String VOLUME_PARAM = "streamVolumeArray";

    @Override
    public void compile(ArgumentContext argument) {
        ParamSpec param = argument.param();
        if (!(argument.value() instanceof Expr.ObjectLiteral object)) {
            argument.error("needs stream levels, for example {MUSIC: 50, RINGER: 80}.");
            return;
        }
        int streamCount = param.constants().size();
        boolean[] changed = new boolean[streamCount];
        int[] volumes = new int[streamCount];
        for (Expr.ObjectEntry entry : object.entries()) {
            Optional<ConstantSpec> stream = param.constant(entry.key());
            if (stream.isEmpty()) {
                argument.error(entry.value(), "has no stream " + entry.key() + ". Use one of: "
                        + EnumKind.listed(EnumKind.constantNames(param)) + ".");
                return;
            }
            if (!(entry.value() instanceof Expr.IntLiteral level) || level.value() < 0 || level.value() > 100) {
                argument.error(entry.value(), "needs a volume percent from 0 to 100.");
                return;
            }
            int index = stream.get().value().asInt();
            changed[index] = true;
            volumes[index] = (int) level.value();
        }
        ArrayNode changedArray = argument.json().arrayNode();
        ArrayNode volumeArray = argument.json().arrayNode();
        for (int i = 0; i < streamCount; i++) {
            changedArray.add(changed[i]);
            volumeArray.add(volumes[i]);
        }
        writeTo(argument, INDEX_PARAM, changedArray);
        writeTo(argument, VOLUME_PARAM, volumeArray);
    }

    private static void writeTo(ArgumentContext argument, String paramName, ArrayNode value) {
        argument.command().param(paramName)
                .ifPresent(target -> argument.writeCompanion(target.jsonField(), value));
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode changed = companionArray(context, INDEX_PARAM);
        JsonNode volumes = companionArray(context, VOLUME_PARAM);
        if (changed == null || volumes == null) {
            return Optional.empty();
        }
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < changed.size() && i < volumes.size(); i++) {
            if (changed.get(i).asBoolean()) {
                int index = i;
                ConstantSpec stream = context.param().constants().stream().filter(c -> c.value().asInt() == index)
                        .findFirst().orElseThrow(() -> context.unrepresentable("stream " + index + " has no name"));
                entries.add(stream.name() + ": " + volumes.get(i).asInt());
            }
        }
        return entries.isEmpty() ? Optional.empty() : Optional.of("{" + String.join(", ", entries) + "}");
    }

    private static JsonNode companionArray(ValueContext context, String paramName) {
        JsonNode node = context.command().param(paramName).map(p -> context.companion(p.jsonField())).orElse(null);
        return node != null && node.isArray() ? node : null;
    }
}
