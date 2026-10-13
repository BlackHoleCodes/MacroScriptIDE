package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.compiler.JsonPaths;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;

/** One parameter value that is being decompiled, and the item it comes from. */
public final class ValueContext {

    private final Decompilation decompilation;
    private final CommandSpec command;
    private final ParamSpec param;
    private final JsonNode item;

    ValueContext(Decompilation decompilation, CommandSpec command, ParamSpec param, JsonNode item) {
        this.decompilation = decompilation;
        this.command = command;
        this.param = param;
        this.item = item;
    }

    public Decompilation decompilation() {
        return decompilation;
    }

    public CommandSpec command() {
        return command;
    }

    public ParamSpec param() {
        return param;
    }

    /** The value of the parameter in the item, or null when the item does not have it. */
    public JsonNode value() {
        return param.hasOutputField() ? JsonPaths.get(item, param.outputPath()) : null;
    }

    /** A companion field next to the parameter's field, or null. */
    public JsonNode companion(String jsonField) {
        String path = param.outputPath() == null ? jsonField : JsonPaths.sibling(param.outputPath(), jsonField);
        return JsonPaths.get(item, path);
    }

    /** Stops the decompilation of the item, which is then written with raw(...). */
    public UnrepresentableException unrepresentable(String reason) {
        return new UnrepresentableException(command.name() + "." + param.name() + ": " + reason);
    }
}
