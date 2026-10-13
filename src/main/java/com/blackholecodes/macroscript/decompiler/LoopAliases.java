package com.blackholecodes.macroscript.decompiler;

import java.util.ArrayDeque;
import java.util.Deque;

/** The key and value names of the for loops around the statement being written. */
final class LoopAliases {

    /** The names of one for loop. */
    record Loop(String key, String value, boolean isArray) {
    }

    private final Deque<Loop> loops = new ArrayDeque<>();

    void push(Loop loop) {
        loops.push(loop);
    }

    void pop() {
        loops.pop();
    }

    Loop innermost() {
        return loops.peek();
    }

    int depth() {
        return loops.size();
    }
}
