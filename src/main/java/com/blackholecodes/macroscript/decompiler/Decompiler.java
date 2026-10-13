package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.compiler.JsonText;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.MappingLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a MacroDroid macro export back into MacroScript. Each exported action block
 * becomes a file of its own.
 *
 * <p>Items that MacroScript cannot write with normal syntax become {@code raw(...)} calls,
 * so compiling the result gives the same macro.
 */
public final class Decompiler {

    private final Mapping mapping;

    public Decompiler() {
        this(MappingLoader.loadDefault());
    }

    public Decompiler(Mapping mapping) {
        this.mapping = mapping;
    }

    /** Decompiles the text of a {@code .macro} or {@code .json} export file. */
    public DecompileResult decompile(String json) throws JsonProcessingException {
        return decompile(JsonText.parse(json));
    }

    /**
     * Decompiles an export. The root can be the export wrapper ({@code {"macro": ...}}) or
     * a macro object.
     *
     * @throws IllegalArgumentException when the JSON is not a MacroDroid macro
     */
    public DecompileResult decompile(JsonNode root) {
        JsonNode macro = root.has("macro") ? root.get("macro") : root;
        if (!macro.isObject() || !macro.has("m_actionList")) {
            throw new IllegalArgumentException("This JSON is not a MacroDroid macro: it has no m_actionList.");
        }
        List<String> warnings = new ArrayList<>();
        List<DecompiledFile> files = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        files.add(decompileOne(macro, warnings, usedNames));
        for (JsonNode block : macro.path("exportedActionBlocks")) {
            files.add(decompileOne(block, warnings, usedNames));
        }
        return new DecompileResult(List.copyOf(files), List.copyOf(warnings));
    }

    private DecompiledFile decompileOne(JsonNode macro, List<String> warnings, Set<String> usedNames) {
        String source = new MacroDecompiler(mapping, warnings).decompile(macro);
        return new DecompiledFile(fileName(macro.path("m_name").asText(""), usedNames), source);
    }

    /** A file name made from a macro name: unusual characters become underscores. */
    static String fileName(String macroName, Set<String> usedNames) {
        String base = macroName.replaceAll("[^A-Za-z0-9 _-]", "_").strip();
        if (base.isEmpty()) {
            base = "macro";
        }
        String name = base;
        for (int i = 2; !usedNames.add(name.toLowerCase(java.util.Locale.ROOT)); i++) {
            name = base + " " + i;
        }
        return name + ".ms";
    }
}
