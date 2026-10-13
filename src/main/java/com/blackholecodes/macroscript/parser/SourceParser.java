package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses MacroScript source text into a {@link MacroFile}.
 *
 * <p>This is the only class outside this package that the rest of the program needs for
 * parsing. It hides the ANTLR classes.
 */
public final class SourceParser {

    private SourceParser() {
    }

    /** Parses a whole {@code .ms} file. */
    public static ParseResult parse(String source) {
        Diagnostics diagnostics = new Diagnostics();
        MacroFile file = parseFile(source, diagnostics);
        return new ParseResult(file, diagnostics.all());
    }

    static MacroFile parseFile(String source, Diagnostics diagnostics) {
        SyntaxErrorListener errors = new SyntaxErrorListener(diagnostics, SourceOrigin.FILE_START);
        MacroScriptLexer lexer = new MacroScriptLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacroScriptParser parser = new MacroScriptParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);
        MacroScriptParser.FileContext tree = parser.file();
        return new AstBuilder(tokens, diagnostics, SourceOrigin.FILE_START).buildFile(tree);
    }

    /**
     * Parses the text of one {@code ${...}} part.
     *
     * @return the expression, or null when the text has a syntax error
     */
    static Expr parseInterpolation(String text, SourceOrigin origin, Diagnostics diagnostics) {
        Diagnostics local = new Diagnostics();
        SyntaxErrorListener errors = new SyntaxErrorListener(local, origin);
        MacroScriptLexer lexer = new MacroScriptLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacroScriptParser parser = new MacroScriptParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);
        MacroScriptParser.InterpolationContext tree = parser.interpolation();
        diagnostics.addAll(local.all());
        if (local.hasErrors()) {
            return null;
        }
        return new AstBuilder(tokens, diagnostics, origin).buildExpression(tree.expression());
    }
}
