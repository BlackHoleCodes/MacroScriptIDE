package com.blackholecodes.macroscript.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collects the errors and warnings of one compilation. */
public final class Diagnostics {

    private final List<Diagnostic> items = new ArrayList<>();

    public void error(SourceRange range, String message) {
        items.add(new Diagnostic(Severity.ERROR, message, range, null));
    }

    public void warning(SourceRange range, String message) {
        items.add(new Diagnostic(Severity.WARNING, message, range, null));
    }

    public void add(Diagnostic diagnostic) {
        items.add(diagnostic);
    }

    public void addAll(List<Diagnostic> diagnostics) {
        items.addAll(diagnostics);
    }

    public boolean hasErrors() {
        return items.stream().anyMatch(Diagnostic::isError);
    }

    public int errorCount() {
        return (int) items.stream().filter(Diagnostic::isError).count();
    }

    /** The messages in source order. */
    public List<Diagnostic> all() {
        List<Diagnostic> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Integer.compare(a.range().start(), b.range().start()));
        return Collections.unmodifiableList(sorted);
    }
}
