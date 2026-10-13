package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.compiler.Compilation;
import com.blackholecodes.macroscript.compiler.JsonPaths;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** One argument that is being compiled, and the item it is written into. */
public final class ArgumentContext {

    private final Compilation compilation;
    private final CommandSpec command;
    private final ParamSpec param;
    private final Expr value;
    private final ObjectNode item;

    public ArgumentContext(Compilation compilation, CommandSpec command, ParamSpec param, Expr value, ObjectNode item) {
        this.compilation = compilation;
        this.command = command;
        this.param = param;
        this.value = value;
        this.item = item;
    }

    public Compilation compilation() {
        return compilation;
    }

    public CommandSpec command() {
        return command;
    }

    public ParamSpec param() {
        return param;
    }

    /** The MacroScript expression given for the argument. */
    public Expr value() {
        return value;
    }

    public JsonNodeFactory json() {
        return JsonNodeFactory.instance;
    }

    /** Writes the value of the parameter to its output field. */
    public void write(JsonNode node) {
        JsonPaths.put(item, param.outputPath(), node);
    }

    /** Writes a companion field, such as the display name that goes with a package name. */
    public void writeCompanion(String jsonField, JsonNode node) {
        String path = param.outputPath() == null ? jsonField : JsonPaths.sibling(param.outputPath(), jsonField);
        JsonPaths.put(item, path, node);
    }

    /** Reports an error about the argument. The message is prefixed with the argument name. */
    public void error(String message) {
        error(value, message);
    }

    /** Reports an error about one part of the argument value. */
    public void error(Expr part, String message) {
        compilation.error(part.range(), "'" + param.name() + "' " + message);
    }
}
