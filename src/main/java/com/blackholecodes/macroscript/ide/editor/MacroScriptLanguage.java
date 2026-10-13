package com.blackholecodes.macroscript.ide.editor;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

/** Registers MacroScript with RSyntaxTextArea: colouring and code folding. */
public final class MacroScriptLanguage {

    /** The syntax style name that editors use for MacroScript. */
    public static final String SYNTAX_STYLE = "text/macroscript";

    private static boolean registered;

    private MacroScriptLanguage() {
    }

    /** Registers the language once. Call it before the first editor is created. */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        AbstractTokenMakerFactory factory = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        factory.putMapping(SYNTAX_STYLE, MacroScriptTokenMaker.class.getName());
        FoldParserManager.get().addFoldParserMapping(SYNTAX_STYLE, new CurlyFoldParser());
        registered = true;
    }
}
