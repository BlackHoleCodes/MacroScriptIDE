package com.blackholecodes.macroscript.decompiler;

import java.util.Optional;

/**
 * Turns an {@code m_expression} such as {@code {lv=j}*2+1} back into MacroScript:
 * {@code j * 2 + 1}.
 */
final class ArithmeticReader {

    private final MagicTextReader magicText;

    ArithmeticReader(MagicTextReader magicText) {
        this.magicText = magicText;
    }

    String read(String expression) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (c == '{') {
                int close = expression.indexOf('}', i);
                // A code with keys has nested braces, so find the brace that matches.
                int depth = 0;
                for (int j = i; j < expression.length(); j++) {
                    if (expression.charAt(j) == '{') {
                        depth++;
                    } else if (expression.charAt(j) == '}' && --depth == 0) {
                        close = j;
                        break;
                    }
                }
                if (close < 0) {
                    throw new UnrepresentableException("Unbalanced braces in the expression " + expression);
                }
                Optional<String> reference = magicText.reference(expression.substring(i + 1, close));
                out.append(reference.orElseThrow(
                        () -> new UnrepresentableException("Unknown magic text in the expression " + expression)));
                i = close + 1;
            } else if ("+-*/%".indexOf(c) >= 0) {
                boolean unary = out.isEmpty() || out.toString().endsWith("(") || out.toString().endsWith(" ");
                out.append(unary ? String.valueOf(c) : " " + c + " ");
                i++;
            } else if (Character.isDigit(c) || c == '.' || c == '(' || c == ')') {
                out.append(c);
                i++;
            } else if (Character.isWhitespace(c)) {
                i++;
            } else {
                throw new UnrepresentableException("The expression " + expression + " has text MacroScript cannot write.");
            }
        }
        return out.toString();
    }
}
