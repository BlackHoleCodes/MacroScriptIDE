package com.blackholecodes.macroscript.decompiler;

import com.blackholecodes.macroscript.mapping.SystemValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Turns MacroDroid magic text back into MacroScript: {@code "Hi {lv=name}"} becomes
 * {@code "Hi ${name}"}. Codes that MacroScript cannot name stay as they are, because the
 * compiler copies text in braces to the output unchanged.
 */
final class MagicTextReader {

    private final Decompilation decompilation;

    MagicTextReader(Decompilation decompilation) {
        this.decompilation = decompilation;
    }

    /** A MacroScript string literal for a magic text value. */
    String stringLiteral(String magicText) {
        StringBuilder out = new StringBuilder("\"");
        int i = 0;
        while (i < magicText.length()) {
            char c = magicText.charAt(i);
            if (c == '\\' && i + 1 < magicText.length() && magicText.charAt(i + 1) == '{') {
                out.append("\\{");
                i += 2;
            } else if (c == '{') {
                int close = matchingBrace(magicText, i);
                Optional<String> reference = close < 0 ? Optional.empty() : reference(magicText.substring(i + 1, close));
                if (reference.isPresent()) {
                    out.append("${").append(reference.get()).append('}');
                    i = close + 1;
                } else {
                    out.append('{');
                    i++;
                }
            } else {
                StringLiterals.appendEscaped(out, magicText, i);
                i++;
            }
        }
        return out.append('"').toString();
    }

    /**
     * The MacroScript expression for text that is exactly one magic text code, for example
     * {@code count} for {@code {lv=count}} or {@code sys.battery} for {@code {battery}}.
     */
    Optional<String> wholeReference(String magicText) {
        if (magicText.length() < 2 || magicText.charAt(0) != '{' || matchingBrace(magicText, 0) != magicText.length() - 1) {
            return Optional.empty();
        }
        return reference(magicText.substring(1, magicText.length() - 1));
    }

    /** The expression for the text inside one pair of braces, or empty when it has none. */
    Optional<String> reference(String code) {
        if (code.startsWith("lv=") || code.startsWith("v=")) {
            return variableReference(code);
        }
        LoopAliases.Loop loop = decompilation.loops().innermost();
        if (loop != null) {
            if (code.equals("iterator_value")) {
                return Optional.of(loop.value());
            }
            if (code.equals(loop.isArray() ? "iterator_array_index" : "iterator_dictionary_key")) {
                return Optional.of(loop.key());
            }
        }
        return decompilation.mapping().systemValueByMagicText("{" + code + "}").map(SystemValue::name).map(name -> "sys." + name);
    }

    private Optional<String> variableReference(String code) {
        boolean global = code.startsWith("v=");
        String rest = code.substring(global ? 2 : 3);
        int bracket = rest.indexOf('[');
        String name = bracket < 0 ? rest : rest.substring(0, bracket);
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return Optional.empty();
        }
        if (global) {
            decompilation.variables().registerName(name);
        } else if (!decompilation.variables().isLocal(name)) {
            return Optional.empty();
        }
        if (bracket < 0) {
            return Optional.of(name);
        }
        Optional<List<String>> keys = keys(rest.substring(bracket));
        return keys.map(list -> name + String.join("", list.stream().map(k -> "[" + k + "]").toList()));
    }

    /** Splits {@code [a][{lv=k}]} into key expressions: {@code "a"} and {@code k}. */
    private Optional<List<String>> keys(String path) {
        List<String> keys = new ArrayList<>();
        int i = 0;
        while (i < path.length()) {
            if (path.charAt(i) != '[') {
                return Optional.empty();
            }
            int close = matchingBracket(path, i);
            if (close < 0) {
                return Optional.empty();
            }
            Optional<String> key = keySource(path.substring(i + 1, close));
            if (key.isEmpty()) {
                return Optional.empty();
            }
            keys.add(key.get());
            i = close + 1;
        }
        return Optional.of(keys);
    }

    /** One dict key as MacroScript: an int, a string, or the reference that holds the key. */
    Optional<String> keySource(String key) {
        if (key.startsWith("{")) {
            return wholeReference(key);
        }
        if (key.matches("0|[1-9]\\d{0,8}")) {
            return Optional.of(key);
        }
        if (key.contains("{")) {
            return Optional.empty();
        }
        return Optional.of(StringLiterals.quote(key));
    }

    private static int matchingBrace(String text, int open) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private static int matchingBracket(String text, int open) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[' || c == '{') {
                depth++;
            } else if ((c == ']' || c == '}') && --depth == 0) {
                return c == ']' ? i : -1;
            }
        }
        return -1;
    }
}
