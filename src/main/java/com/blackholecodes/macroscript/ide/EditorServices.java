package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.ide.editor.EditorTheme;
import org.fife.ui.autocomplete.CompletionProvider;

import java.nio.file.Path;
import java.util.List;

/** What an editor tab needs from the rest of the IDE. */
interface EditorServices {

    /** Compiles the text of a file for the live error underlines. */
    CompileResult compile(Path file, String text);

    CompletionProvider completions();

    EditorTheme theme();

    int fontSize();

    /** Called on the event thread when the messages of a tab change. */
    void diagnosticsChanged(EditorTab tab, List<Diagnostic> diagnostics);

    /** Called when a tab's text changes from saved to modified or back. */
    void dirtyChanged(EditorTab tab);

    /** Called when the caret of a tab moves. */
    void caretMoved(EditorTab tab);
}
