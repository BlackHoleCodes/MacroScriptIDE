package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.StringPart;
import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the text of a string literal token: it resolves escapes and splits the text at each
 * {@code ${...}} part. The text inside a {@code ${...}} part is parsed as an expression.
 */
final class StringLiteralReader {

    private final Token token;
    private final String text;
    private final SourceOrigin origin;
    private final Diagnostics diagnostics;
    private final List<StringPart> parts = new ArrayList<>();
    private final StringBuilder pendingText = new StringBuilder();

    StringLiteralReader(Token token, SourceOrigin origin, Diagnostics diagnostics) {
        this.token = token;
        this.text = token.getText();
        this.origin = origin;
        this.diagnostics = diagnostics;
    }

    Expr.StringLiteral read() {
        // The token text includes the quotes. Index 0 is the opening quote.
        int end = text.length() - 1;
        int i = 1;
        while (i < end) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < end) {
                readEscape(i);
                i += 2;
            } else if (c == '$' && i + 1 < end && text.charAt(i + 1) == '{') {
                i = readInterpolation(i);
            } else {
                pendingText.append(c);
                i++;
            }
        }
        flushText();
        return new Expr.StringLiteral(List.copyOf(parts), origin.range(token));
    }

    private void readEscape(int index) {
        char escaped = text.charAt(index + 1);
        switch (escaped) {
            case '"', '\\', '$' -> pendingText.append(escaped);
            case 'n' -> pendingText.append('\n');
            case 't' -> pendingText.append('\t');
            case '{' -> {
                flushText();
                parts.add(new StringPart.EscapedBrace());
            }
            default -> diagnostics.error(rangeAt(index, 2),
                    "Unknown escape \\" + escaped + ". Use \\\", \\\\, \\n, \\t, \\$ or \\{.");
        }
    }

    /** Reads a {@code ${...}} part that starts at the dollar sign and returns the index after it. */
    private int readInterpolation(int dollarIndex) {
        int contentStart = dollarIndex + 2;
        int close = findClosingBrace(contentStart);
        if (close < 0) {
            diagnostics.error(rangeAt(dollarIndex, 2), "This ${ has no closing }. Write \\${ for a literal ${.");
            pendingText.append("${");
            return contentStart;
        }
        String content = text.substring(contentStart, close);
        if (content.isBlank()) {
            diagnostics.error(rangeAt(dollarIndex, close - dollarIndex + 1), "Write a variable name inside ${ }.");
        } else {
            Expr expression = SourceParser.parseInterpolation(content, origin.inside(token, contentStart), diagnostics);
            if (expression != null) {
                flushText();
                parts.add(new StringPart.Interpolation(expression));
            }
        }
        return close + 1;
    }

    /** Finds the brace that closes a ${ part. Quotes inside the part are skipped. */
    private int findClosingBrace(int from) {
        boolean inQuotes = false;
        for (int i = from; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '}' && !inQuotes) {
                return i;
            }
        }
        return -1;
    }

    private void flushText() {
        if (!pendingText.isEmpty()) {
            parts.add(new StringPart.Text(pendingText.toString()));
            pendingText.setLength(0);
        }
    }

    private SourceRange rangeAt(int index, int length) {
        SourceOrigin at = origin.inside(token, index);
        return new SourceRange(at.offset(), at.offset() + length, at.line(), at.column());
    }
}
