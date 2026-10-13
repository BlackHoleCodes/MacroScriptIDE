package com.blackholecodes.macroscript.build;

import com.blackholecodes.macroscript.compiler.JsonText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildServiceTest {

    private static final String BLOCK = """
            @actionBlock
            @name("Beep")
            @description("x")
            actions { vibrate(SHORT_BUZZ); }
            """;
    private static final String MACRO = """
            @name("Caller")
            @description("x")
            triggers { manual(); }
            actions { callBlock("Beep"); }
            """;

    private final BuildService builds = new BuildService();

    @Test
    void buildsAMacroWithAnActionBlockFromTheProject(@TempDir Path project) throws Exception {
        Files.createDirectories(project.resolve("blocks"));
        Files.writeString(project.resolve("blocks/beep.ms"), BLOCK);
        Path macro = Files.writeString(project.resolve("caller.ms"), MACRO);

        assertEquals(List.of(macro), builds.macroFiles(project), "action block files are not built on their own");

        BuildResult result = builds.build(macro, project, project.resolve("out"), path -> Optional.empty());
        assertTrue(result.succeeded());
        assertEquals(project.resolve("out/caller.json"), result.outputFile());
        var json = JsonText.parse(Files.readString(result.outputFile()));
        assertEquals("Beep", json.path("macro").path("exportedActionBlocks").get(0).path("m_name").asText());
    }

    @Test
    void usesTheEditorTextOfOpenFiles(@TempDir Path project) throws Exception {
        Path macro = Files.writeString(project.resolve("m.ms"), MACRO);
        Path block = Files.writeString(project.resolve("b.ms"), "// not written yet");
        BuildResult result = builds.build(macro, project, project.resolve("out"),
                path -> path.equals(block) ? Optional.of(BLOCK) : Optional.empty());
        assertTrue(result.succeeded());
    }

    @Test
    void writesNothingWhenThereAreErrors(@TempDir Path project) throws Exception {
        Path macro = Files.writeString(project.resolve("bad.ms"), "triggers { manual(); }\nactions { nope(); }");
        BuildResult result = builds.build(macro, project, project.resolve("out"), path -> Optional.empty());
        assertFalse(result.succeeded());
        assertFalse(Files.exists(project.resolve("out/bad.json")));
    }

    @Test
    void decompilesWithoutOverwritingFiles(@TempDir Path project) throws Exception {
        Path macro = Files.writeString(project.resolve("caller.ms"), MACRO);
        Files.writeString(project.resolve("beep.ms"), BLOCK);
        Path json = builds.build(macro, project, project.resolve("out"), path -> Optional.empty()).outputFile();

        BuildService.DecompileOutcome first = builds.decompile(json, project.resolve("imported"));
        assertEquals(List.of(project.resolve("imported/Caller.ms"), project.resolve("imported/Beep.ms")), first.files());
        BuildService.DecompileOutcome second = builds.decompile(json, project.resolve("imported"));
        assertEquals(project.resolve("imported/Caller (2).ms"), second.files().get(0));
    }
}
