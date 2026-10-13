package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.MappingLoader;
import com.blackholecodes.macroscript.parser.ParseResult;
import com.blackholecodes.macroscript.parser.SourceParser;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles MacroScript files to MacroDroid export JSON. This is the entry point of the
 * compiler for the IDE and the command line.
 *
 * <pre>{@code
 * CompileResult result = new Compiler().compile(new SourceUnit(path, text), new DirectoryActionBlockResolver(projectRoot));
 * if (!result.hasErrors()) {
 *     Files.writeString(outputPath, result.outputText());
 * }
 * }</pre>
 */
public final class Compiler {

    /** The value of macroExportVersion in the output wrapper. */
    private static final int EXPORT_VERSION = 1;

    private final Mapping mapping;

    /** A compiler that uses the mapping file that ships with the program. */
    public Compiler() {
        this(MappingLoader.loadDefault());
    }

    public Compiler(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Compiles one file.
     *
     * @param resolver finds the files of the action blocks that the file calls
     */
    public CompileResult compile(SourceUnit unit, ActionBlockResolver resolver) {
        Diagnostics diagnostics = new Diagnostics();
        ParseResult parsed = SourceParser.parse(unit.text());
        diagnostics.addAll(parsed.diagnostics());

        GuidAllocator guids = new GuidAllocator();
        ActionBlockLinker linker = new ActionBlockLinker(resolver, mapping, guids);
        Compilation compilation = new Compilation(mapping, diagnostics, linker, guids);
        ObjectNode macro = new MacroCompiler(compilation).compile(parsed.file(), unit.baseName());

        List<ObjectNode> blocks = linker.exportedBlocks();
        if (!blocks.isEmpty()) {
            ArrayNode exported = macro.putArray("exportedActionBlocks");
            blocks.forEach(exported::add);
        }

        List<Diagnostic> all = new ArrayList<>();
        diagnostics.all().forEach(d -> all.add(d.inFile(unit.path())));
        all.addAll(linker.blockDiagnostics());
        boolean failed = all.stream().anyMatch(Diagnostic::isError);
        return new CompileResult(failed ? null : wrap(macro), List.copyOf(all));
    }

    /** Compiles text that calls no action blocks. */
    public CompileResult compile(String text) {
        return compile(new SourceUnit(null, text), ActionBlockResolver.NONE);
    }

    private static ObjectNode wrap(ObjectNode macro) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("macroExportVersion", EXPORT_VERSION);
        root.set("macro", macro);
        root.putArray("globalVariables");
        root.putNull("userIcons");
        return root;
    }

    public Mapping mapping() {
        return mapping;
    }
}
