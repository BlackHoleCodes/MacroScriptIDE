package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.mapping.SystemValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Finds out what a name, a {@code sys.} value or an indexed name in an expression refers to. */
public final class ReferenceResolver {

    private final Compilation compilation;

    ReferenceResolver(Compilation compilation) {
        this.compilation = compilation;
    }

    /** True when the expression has the form of a reference (a name, sys value or index). */
    public static boolean looksLikeReference(Expr expr) {
        return switch (expr) {
            case Expr.Name n -> true;
            case Expr.SysValue s -> true;
            case Expr.Index i -> looksLikeReference(i.target());
            case Expr.Cast c -> looksLikeReference(c.operand());
            default -> false;
        };
    }

    /**
     * Resolves a reference and reports an error when it cannot.
     *
     * @return the reference, or empty after an error was reported
     */
    public Optional<Reference> resolve(Expr expr) {
        return switch (expr) {
            case Expr.Name name -> resolveName(name);
            case Expr.SysValue sys -> resolveSystem(sys);
            case Expr.Index index -> resolveIndex(index);
            case Expr.Cast cast -> resolve(cast.operand());
            default -> {
                compilation.error(expr.range(), "Expected a variable name or a sys value here.");
                yield Optional.empty();
            }
        };
    }

    /** Resolves a reference that must be a declared variable (optionally with keys). */
    public Optional<Reference.Variable> resolveVariable(Expr expr) {
        Optional<Reference> reference = resolve(expr);
        if (reference.isEmpty()) {
            return Optional.empty();
        }
        if (reference.get() instanceof Reference.Variable variable) {
            return Optional.of(variable);
        }
        compilation.error(expr.range(), "Expected a declared variable here.");
        return Optional.empty();
    }

    private Optional<Reference> resolveName(Expr.Name name) {
        Scope scope = compilation.scope();
        Optional<LoopScope> loop = scope.innermostLoop();
        if (loop.isPresent()) {
            if (loop.get().keyAlias().equals(name.name())) {
                return Optional.of(new Reference.LoopKey(loop.get()));
            }
            if (loop.get().valueAlias().equals(name.name())) {
                return Optional.of(new Reference.LoopValue(loop.get()));
            }
        }
        if (scope.isOuterLoopAlias(name.name())) {
            compilation.error(name.range(), "'" + name.name() + "' belongs to an outer for loop. Inside a nested loop "
                    + "MacroDroid gives only the key and value of the innermost loop. Copy the value to a variable first.");
            return Optional.empty();
        }
        Optional<VariableSymbol> symbol = scope.variable(name.name());
        if (symbol.isPresent()) {
            return Optional.of(new Reference.Variable(symbol.get(), List.of()));
        }
        List<String> known = new ArrayList<>();
        scope.variables().forEach(v -> known.add(v.name()));
        compilation.error(name.range(), "'" + name.name() + "' is not declared. Declare it in the variables block."
                + Suggestions.didYouMean(name.name(), known));
        return Optional.empty();
    }

    private Optional<Reference> resolveSystem(Expr.SysValue sys) {
        Optional<SystemValue> value = compilation.mapping().systemValue(sys.name());
        if (value.isPresent()) {
            return Optional.of(new Reference.System(value.get()));
        }
        List<String> names = compilation.mapping().systemValues().stream().map(SystemValue::name).toList();
        compilation.error(sys.range(), "Unknown system value 'sys." + sys.name() + "'."
                + Suggestions.didYouMean(sys.name(), names));
        return Optional.empty();
    }

    private Optional<Reference> resolveIndex(Expr.Index index) {
        List<Expr> keys = new ArrayList<>();
        Expr base = index;
        while (base instanceof Expr.Index inner) {
            keys.add(inner.index());
            base = inner.target();
        }
        Collections.reverse(keys);
        if (!(base instanceof Expr.Name name)) {
            compilation.error(base.range(), "Only a dict or array variable can be indexed.");
            return Optional.empty();
        }
        Optional<Reference> target = resolveName(name);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        if (!(target.get() instanceof Reference.Variable variable) || !variable.symbol().type().isCollection()) {
            compilation.error(name.range(), "'" + name.name() + "' is not a dict or array, so it cannot be indexed.");
            return Optional.empty();
        }
        return Optional.of(new Reference.Variable(variable.symbol(), List.copyOf(keys)));
    }
}
