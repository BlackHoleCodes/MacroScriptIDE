package com.blackholecodes.macroscript.ast;

import java.util.List;
import java.util.Optional;

/**
 * A parsed {@code .ms} file. A block that is not in the file is null.
 */
public record MacroFile(List<Annotation> annotations,
                        Block<Declaration> variables,
                        Block<TriggerStatement> triggers,
                        Block<ConstraintStatement> constraints,
                        Block<Stmt> actions) {

    public Optional<Annotation> annotation(String name) {
        return annotations.stream().filter(a -> a.name().equals(name)).findFirst();
    }

    /** True when the file has {@code @actionBlock}. */
    public boolean isActionBlock() {
        return annotation("actionBlock").isPresent();
    }

    /** The text of {@code @name("...")}, if the file has one with a plain string. */
    public Optional<String> declaredName() {
        return annotation("name")
                .map(Annotation::argument)
                .filter(arg -> arg instanceof Expr.StringLiteral)
                .flatMap(arg -> ((Expr.StringLiteral) arg).plainText());
    }

    public List<Declaration> declarations() {
        return variables == null ? List.of() : variables.items();
    }
}
