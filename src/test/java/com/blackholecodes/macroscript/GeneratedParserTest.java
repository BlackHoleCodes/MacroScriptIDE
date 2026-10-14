package com.blackholecodes.macroscript;

import org.antlr.v4.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The lexer and parser in src/main/antlr-generated are committed, so the project compiles in
 * an IDE that does not run the ANTLR Maven plugin. This test regenerates them from the
 * grammar and fails if the committed copies are out of date. To update them, run
 * {@code ./mvnw generate-sources}.
 */
class GeneratedParserTest {

    private static final Path GRAMMAR = Path.of("src/main/antlr4/com/blackholecodes/macroscript/parser/MacroScript.g4");
    private static final Path COMMITTED = Path.of("src/main/antlr-generated/com/blackholecodes/macroscript/parser");
    private static final List<String> GENERATED_FILES = List.of("MacroScriptLexer.java", "MacroScriptParser.java");

    @Test
    void committedParserMatchesTheGrammar(@TempDir Path output) throws IOException {
        Tool tool = new Tool(new String[] {
                "-o", output.toString(), "-Xexact-output-dir",
                "-package", "com.blackholecodes.macroscript.parser",
                "-no-visitor", "-no-listener",
                GRAMMAR.toString()});
        tool.processGrammarsOnCommandLine();
        assertEquals(0, tool.getNumErrors(), "The grammar has errors");

        for (String file : GENERATED_FILES) {
            assertEquals(withoutHeader(output.resolve(file)), withoutHeader(COMMITTED.resolve(file)),
                    file + " is out of date. Run ./mvnw generate-sources and commit src/main/antlr-generated.");
        }
    }

    /** The file text without its first line, which names the grammar path ANTLR was given. */
    private static String withoutHeader(Path file) throws IOException {
        String text = Files.readString(file).replace("\r\n", "\n");
        return text.substring(text.indexOf('\n') + 1);
    }
}
