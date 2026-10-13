package com.blackholecodes.macroscript.cli;

import com.blackholecodes.macroscript.build.BuildResult;
import com.blackholecodes.macroscript.build.BuildService;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The command line interface:
 *
 * <pre>
 * compile &lt;file.ms | folder&gt;... [-o &lt;output folder&gt;]
 * decompile &lt;export.json&gt; [-o &lt;folder&gt;]
 * </pre>
 */
public final class CommandLine {

    static final String USAGE = """
            Usage:
              macroscript                                     Start the IDE
              macroscript <project folder>                    Start the IDE with a project open
              macroscript compile <file.ms | folder>... [-o <output folder>]
              macroscript decompile <export.json> [-o <folder>]

            compile writes one .json file per macro (default folder: out/ next to the sources).
            decompile writes one .ms file for the macro and one per action block.""";

    private final PrintStream out;
    private final PrintStream err;
    private final BuildService builds = new BuildService();

    public CommandLine(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    /** True when the arguments ask for a command line action instead of the IDE. */
    public static boolean handles(String[] args) {
        return args.length > 0 && List.of("compile", "decompile", "help", "--help", "-h").contains(args[0]);
    }

    /** Runs a command and returns the process exit code. */
    public int run(String[] args) {
        try {
            return switch (args[0]) {
                case "compile" -> compile(args);
                case "decompile" -> decompile(args);
                default -> {
                    out.println(USAGE);
                    yield 0;
                }
            };
        } catch (IllegalArgumentException e) {
            err.println(e.getMessage());
            err.println(USAGE);
            return 2;
        } catch (IOException e) {
            err.println("error: " + e.getMessage());
            return 1;
        }
    }

    private int compile(String[] args) throws IOException {
        Arguments parsed = Arguments.parse(args);
        int failures = 0;
        for (Path input : parsed.inputs()) {
            Path root = Files.isDirectory(input) ? input : parent(input);
            List<Path> files = Files.isDirectory(input) ? builds.macroFiles(input) : List.of(input);
            Path outputFolder = parsed.outputFolder().orElse(root.resolve("out"));
            for (Path file : files) {
                BuildResult result = builds.build(file, root, outputFolder, path -> Optional.empty());
                for (Diagnostic diagnostic : result.compile().diagnostics()) {
                    err.println(diagnostic.inFile(file).format());
                }
                if (result.succeeded()) {
                    out.println("Compiled " + file + " -> " + result.outputFile());
                } else {
                    failures++;
                }
            }
        }
        return failures == 0 ? 0 : 1;
    }

    private int decompile(String[] args) throws IOException {
        Arguments parsed = Arguments.parse(args);
        for (Path input : parsed.inputs()) {
            Path folder = parsed.outputFolder().orElse(parent(input));
            BuildService.DecompileOutcome outcome = builds.decompile(input, folder);
            outcome.warnings().forEach(w -> err.println("warning: " + w));
            outcome.files().forEach(f -> out.println("Wrote " + f));
        }
        return 0;
    }

    private static Path parent(Path file) {
        Path parent = file.toAbsolutePath().getParent();
        return parent != null ? parent : Path.of(".");
    }

    /** The files and the -o option after the command name. */
    private record Arguments(List<Path> inputs, Optional<Path> outputFolder) {

        static Arguments parse(String[] args) {
            List<Path> inputs = new ArrayList<>();
            Path output = null;
            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-o")) {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("-o needs a folder.");
                    }
                    output = Path.of(args[++i]);
                } else {
                    inputs.add(Path.of(args[i]));
                }
            }
            if (inputs.isEmpty()) {
                throw new IllegalArgumentException(args[0] + " needs at least one file.");
            }
            for (Path input : inputs) {
                if (!Files.exists(input)) {
                    throw new IllegalArgumentException("File not found: " + input);
                }
            }
            return new Arguments(List.copyOf(inputs), Optional.ofNullable(output));
        }
    }
}
