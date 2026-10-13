package com.blackholecodes.macroscript.ast;

import com.blackholecodes.macroscript.diagnostics.SourceRange;

import java.util.List;

/** A statement of the actions block. */
public sealed interface Stmt {

    ItemMeta meta();

    SourceRange range();

    /** A call such as {@code setWifi(TURN_ON) when (flag);}. {@code when} is null when absent. */
    record CallStatement(Expr.Call call, Expr when, ItemMeta meta, SourceRange range) implements Stmt {
    }

    /** The left side of an assignment: a variable, optionally with dict or array keys. */
    record Target(String name, List<Expr> keys, SourceRange nameRange, SourceRange range) {

        public boolean isEntry() {
            return !keys.isEmpty();
        }
    }

    /** An assignment. {@code value} is null for {@code ++} and {@code --}. */
    record Assignment(Target target, AssignOp op, Expr value, ItemMeta meta, SourceRange range) implements Stmt {
    }

    /** One {@code if} or {@code else if} branch. */
    record Branch(Expr condition, List<Stmt> body, SourceRange range) {
    }

    /** An if chain. {@code elseBody} is null when there is no else. */
    record If(List<Branch> branches, List<Stmt> elseBody, ItemMeta meta, SourceRange range) implements Stmt {
    }

    record While(Expr condition, List<Stmt> body, ItemMeta meta, SourceRange range) implements Stmt {
    }

    record DoWhile(List<Stmt> body, Expr condition, ItemMeta meta, SourceRange range) implements Stmt {
    }

    /** {@code repeat (count)} or {@code repeat (duration)}. */
    record Repeat(Expr count, List<Stmt> body, ItemMeta meta, SourceRange range) implements Stmt {
    }

    /** {@code for (key, value : source)}. */
    record For(String keyAlias, String valueAlias, Expr source, List<Stmt> body, ItemMeta meta,
               SourceRange range) implements Stmt {
    }

    record Break(ItemMeta meta, SourceRange range) implements Stmt {
    }

    record Continue(ItemMeta meta, SourceRange range) implements Stmt {
    }

    record Return(ItemMeta meta, SourceRange range) implements Stmt {
    }
}
