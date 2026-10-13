package com.blackholecodes.macroscript.ide.editor;

import com.blackholecodes.macroscript.parser.MacroScriptLexer;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The keywords of MacroScript, read from the lexer that ANTLR generates from the grammar.
 * The grammar is the only list of keywords, so the editor and the compiler cannot disagree.
 */
public final class Keywords {

    private static final Map<String, Integer> KEYWORDS = readKeywords();

    private Keywords() {
    }

    /** Every keyword, in grammar order. */
    public static Set<String> all() {
        return KEYWORDS.keySet();
    }

    /** True when the lexer token type is a keyword. */
    public static boolean isKeywordToken(int tokenType) {
        return KEYWORDS.containsValue(tokenType);
    }

    /** A keyword is a token whose text in the grammar is a fixed lower-case word, such as 'if'. */
    private static Map<String, Integer> readKeywords() {
        Vocabulary vocabulary = MacroScriptLexer.VOCABULARY;
        Map<String, Integer> keywords = new LinkedHashMap<>();
        for (int type = 1; type <= vocabulary.getMaxTokenType(); type++) {
            String literal = vocabulary.getLiteralName(type);
            if (literal != null && literal.matches("'[a-z]+'")) {
                keywords.put(literal.substring(1, literal.length() - 1), type);
            }
        }
        return Collections.unmodifiableMap(keywords);
    }
}
