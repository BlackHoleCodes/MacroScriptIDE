package com.blackholecodes.macroscript.ide.editor;

import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.Color;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Runs the compiler on the editor text after each pause in typing, and shows the errors
 * and warnings as wavy underlines with the message as tool tip.
 */
public final class DiagnosticsParser extends AbstractParser {

    private static final Color ERROR_COLOR = new Color(0xE0, 0x40, 0x40);
    private static final Color WARNING_COLOR = new Color(0xD0, 0x9A, 0x20);

    private final Function<String, CompileResult> compile;
    private final Consumer<List<Diagnostic>> listener;
    private Path file;

    /**
     * @param compile  compiles the editor text
     * @param listener gets the messages for this file after each run, on the event thread
     */
    public DiagnosticsParser(Path file, Function<String, CompileResult> compile, Consumer<List<Diagnostic>> listener) {
        this.file = file;
        this.compile = compile;
        this.listener = listener;
    }

    /** Called when the file is saved under a new name. */
    public void setFile(Path file) {
        this.file = file;
    }

    @Override
    public ParseResult parse(RSyntaxDocument document, String style) {
        DefaultParseResult result = new DefaultParseResult(this);
        long start = System.currentTimeMillis();
        String text;
        try {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException e) {
            result.setError(e);
            return result;
        }
        List<Diagnostic> diagnostics;
        try {
            diagnostics = compile.apply(text).diagnostics().stream().filter(this::isForThisFile).toList();
        } catch (RuntimeException e) {
            // A bug in the compiler must not break the editor. Show it as one message.
            diagnostics = List.of();
            result.setError(e);
        }
        Element root = document.getDefaultRootElement();
        for (Diagnostic diagnostic : diagnostics) {
            int offset = Math.min(diagnostic.range().start(), Math.max(0, document.getLength() - 1));
            int length = Math.max(1, Math.min(diagnostic.range().length(), document.getLength() - offset));
            int line = root.getElementIndex(offset);
            DefaultParserNotice notice = new DefaultParserNotice(this, diagnostic.message(), line, offset, length);
            notice.setLevel(diagnostic.isError() ? ParserNotice.Level.ERROR : ParserNotice.Level.WARNING);
            notice.setColor(diagnostic.isError() ? ERROR_COLOR : WARNING_COLOR);
            notice.setShowInEditor(true);
            result.addNotice(notice);
        }
        result.setParseTime(System.currentTimeMillis() - start);
        result.setParsedLines(0, root.getElementCount() - 1);
        List<Diagnostic> forListener = diagnostics;
        SwingUtilities.invokeLater(() -> listener.accept(forListener));
        return result;
    }

    private boolean isForThisFile(Diagnostic diagnostic) {
        return diagnostic.file() == null || diagnostic.file().equals(file);
    }
}
