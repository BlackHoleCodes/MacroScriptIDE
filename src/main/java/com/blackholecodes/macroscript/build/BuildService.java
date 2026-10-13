package com.blackholecodes.macroscript.build;

import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.compiler.Compiler;
import com.blackholecodes.macroscript.compiler.DirectoryActionBlockResolver;
import com.blackholecodes.macroscript.compiler.SourceUnit;
import com.blackholecodes.macroscript.decompiler.DecompileResult;
import com.blackholecodes.macroscript.decompiler.DecompiledFile;
import com.blackholecodes.macroscript.decompiler.Decompiler;
import com.blackholecodes.macroscript.parser.SourceParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Compiles {@code .ms} files to JSON files and decompiles exports to {@code .ms} files.
 * The IDE and the command line both use this class, so they behave the same way.
 */
public final class BuildService {

    /** The extension of compiled macros. MacroDroid imports this JSON file. */
    public static final String OUTPUT_EXTENSION = ".json";
    public static final String SOURCE_EXTENSION = ".ms";

    private final Compiler compiler = new Compiler();
    private final Decompiler decompiler = new Decompiler(compiler.mapping());

    /**
     * Compiles one file and writes the JSON next to the other outputs.
     *
     * @param projectRoot  the folder that is searched for action blocks
     * @param outputFolder the folder for the JSON file
     * @param openFileText gives the editor text of open files, so unsaved changes are used
     */
    public BuildResult build(Path source, Path projectRoot, Path outputFolder,
                             Function<Path, Optional<String>> openFileText) throws IOException {
        String text = openFileText.apply(source).orElse(Files.readString(source));
        CompileResult result = compile(source, text, projectRoot, openFileText);
        if (result.hasErrors()) {
            return new BuildResult(source, null, result);
        }
        Files.createDirectories(outputFolder);
        Path output = outputFolder.resolve(baseName(source) + OUTPUT_EXTENSION);
        Files.writeString(output, result.outputText());
        return new BuildResult(source, output, result);
    }

    /** Compiles text without writing anything. The editor uses this to show errors as you type. */
    public CompileResult compile(Path source, String text, Path projectRoot, Function<Path, Optional<String>> openFileText) {
        return compiler.compile(new SourceUnit(source, text), new DirectoryActionBlockResolver(projectRoot, openFileText));
    }

    /**
     * The {@code .ms} files of a project that are macros. Action block files are left out,
     * because they are compiled into the macros that call them.
     */
    public List<Path> macroFiles(Path projectRoot) throws IOException {
        List<Path> macros = new ArrayList<>();
        try (Stream<Path> files = Files.walk(projectRoot)) {
            for (Path file : files.filter(BuildService::isSourceFile).sorted().toList()) {
                MacroFile parsed = SourceParser.parse(Files.readString(file)).file();
                if (!parsed.isActionBlock()) {
                    macros.add(file);
                }
            }
        }
        return macros;
    }

    /**
     * Decompiles an export and writes one {@code .ms} file for the macro and one for each
     * action block. Existing files are not overwritten: a number is added to the name.
     *
     * @return the files written, the macro first
     */
    public DecompileOutcome decompile(Path exportFile, Path targetFolder) throws IOException {
        DecompileResult result = decompiler.decompile(Files.readString(exportFile));
        Files.createDirectories(targetFolder);
        List<Path> written = new ArrayList<>();
        for (DecompiledFile file : result.files()) {
            Path target = freeName(targetFolder, file.fileName());
            Files.writeString(target, file.source());
            written.add(target);
        }
        return new DecompileOutcome(written, result.warnings());
    }

    /** The files written by {@link #decompile} and the warnings to show. */
    public record DecompileOutcome(List<Path> files, List<String> warnings) {
    }

    public static boolean isSourceFile(Path path) {
        return path.getFileName().toString().endsWith(SOURCE_EXTENSION) && Files.isRegularFile(path);
    }

    public static String baseName(Path source) {
        return new SourceUnit(source, "").baseName();
    }

    private static Path freeName(Path folder, String fileName) {
        Path target = folder.resolve(fileName);
        String stem = fileName.endsWith(SOURCE_EXTENSION) ? fileName.substring(0, fileName.length() - 3) : fileName;
        for (int i = 2; Files.exists(target); i++) {
            target = folder.resolve(stem + " (" + i + ")" + SOURCE_EXTENSION);
        }
        return target;
    }
}
