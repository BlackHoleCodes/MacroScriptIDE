package com.blackholecodes.macroscript.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final CommandLine commandLine = new CommandLine(new PrintStream(out, true), new PrintStream(err, true));

    @Test
    void recognisesItsCommands() {
        assertTrue(CommandLine.handles(new String[] {"compile", "x.ms"}));
        assertFalse(CommandLine.handles(new String[] {}));
        assertFalse(CommandLine.handles(new String[] {"/some/project"}));
    }

    @Test
    void compilesAndDecompiles(@TempDir Path folder) throws Exception {
        Path source = Files.writeString(folder.resolve("hello.ms"),
                "@description(\"x\")\ntriggers { manual(); }\nactions { toast(\"Hello\"); }\n");
        assertEquals(0, commandLine.run(new String[] {"compile", source.toString()}));
        Path json = folder.resolve("out/hello.json");
        assertTrue(Files.exists(json));

        assertEquals(0, commandLine.run(new String[] {"decompile", json.toString(), "-o", folder.resolve("back").toString()}));
        assertTrue(Files.readString(folder.resolve("back/hello.ms")).contains("toast(\"Hello\");"));
    }

    @Test
    void reportsErrorsWithFileAndLine(@TempDir Path folder) throws Exception {
        Path source = Files.writeString(folder.resolve("bad.ms"), "@description(\"x\")\ntriggers { manual(); }\nactions { nope(); }\n");
        assertEquals(1, commandLine.run(new String[] {"compile", source.toString()}));
        assertTrue(err.toString().contains("bad.ms:3:11: error: Unknown action 'nope'"), err.toString());
    }

    @Test
    void explainsMissingArguments() {
        assertEquals(2, commandLine.run(new String[] {"compile"}));
        assertTrue(err.toString().contains("Usage:"));
    }
}
