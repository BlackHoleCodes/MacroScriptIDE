package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Annotation;
import com.blackholecodes.macroscript.ast.Expr;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Reads the file annotations: {@code @name}, {@code @description}, {@code @category} and so on. */
final class AnnotationCompiler {

    private static final List<String> FILE_ANNOTATIONS = List.of("name", "description", "category", "enabled",
            "favourite", "headingColor", "noLog", "logLevel", "actionBlock");
    private static final String DEFAULT_CATEGORY = "Uncategorized";

    private final Compilation compilation;

    AnnotationCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    MacroSettings compile(List<Annotation> annotations, String defaultName) {
        String name = defaultName;
        String description = null;
        String category = DEFAULT_CATEGORY;
        boolean enabled = true;
        boolean favourite = false;
        Integer headingColor = null;
        boolean noLog = false;
        int logLevel = 0;
        boolean actionBlock = false;

        Set<String> seen = new HashSet<>();
        for (Annotation annotation : annotations) {
            if (!seen.add(annotation.name())) {
                compilation.error(annotation.range(), "@" + annotation.name() + " is given twice.");
                continue;
            }
            switch (annotation.name()) {
                case "name" -> name = string(annotation).orElse(name);
                case "description" -> description = string(annotation).orElse(description);
                case "category" -> category = string(annotation).orElse(category);
                case "enabled" -> enabled = bool(annotation).orElse(enabled);
                case "favourite" -> favourite = flag(annotation);
                case "headingColor" -> headingColor = color(annotation).orElse(null);
                case "noLog" -> noLog = flag(annotation);
                case "logLevel" -> logLevel = logLevel(annotation).orElse(logLevel);
                case "actionBlock" -> actionBlock = flag(annotation);
                case "disabled", "comment" -> compilation.error(annotation.range(), "@" + annotation.name()
                        + " goes on the line before a trigger, constraint or action, not at the top of the file.");
                default -> compilation.error(annotation.range(), "Unknown annotation @" + annotation.name() + "."
                        + Suggestions.didYouMean(annotation.name(), FILE_ANNOTATIONS));
            }
        }
        if (description == null) {
            compilation.warning(annotations.isEmpty() ? com.blackholecodes.macroscript.diagnostics.SourceRange.NONE
                    : annotations.get(0).range(), "The macro has no @description.");
            description = "";
        }
        return new MacroSettings(name, description, category, enabled, favourite, headingColor, noLog, logLevel, actionBlock);
    }

    private Optional<String> string(Annotation annotation) {
        if (annotation.argument() instanceof Expr.StringLiteral literal && !literal.hasInterpolation()) {
            return literal.plainText();
        }
        compilation.error(annotation.range(), "@" + annotation.name() + " needs a string: @" + annotation.name() + "(\"...\").");
        return Optional.empty();
    }

    private Optional<Boolean> bool(Annotation annotation) {
        if (annotation.argument() instanceof Expr.BoolLiteral bool) {
            return Optional.of(bool.value());
        }
        compilation.error(annotation.range(), "@" + annotation.name() + " needs true or false.");
        return Optional.empty();
    }

    /** An annotation with no value, such as {@code @favourite}. */
    private boolean flag(Annotation annotation) {
        if (annotation.argument() != null) {
            compilation.error(annotation.range(), "@" + annotation.name() + " takes no value.");
        }
        return true;
    }

    private Optional<Integer> color(Annotation annotation) {
        if (annotation.argument() instanceof Expr.ColorLiteral color) {
            return Optional.of(color.argb());
        }
        compilation.error(annotation.range(), "@headingColor needs a color, for example @headingColor(#FF2196F3).");
        return Optional.empty();
    }

    private Optional<Integer> logLevel(Annotation annotation) {
        if (annotation.argument() instanceof Expr.Name level) {
            switch (level.name()) {
                case "FULL" -> {
                    return Optional.of(0);
                }
                case "TRIGGERS_ONLY" -> {
                    return Optional.of(1);
                }
                default -> {
                    // Reported below.
                }
            }
        }
        compilation.error(annotation.range(), "@logLevel needs FULL or TRIGGERS_ONLY.");
        return Optional.empty();
    }
}
