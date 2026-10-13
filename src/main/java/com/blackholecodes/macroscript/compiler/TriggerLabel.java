package com.blackholecodes.macroscript.compiler;

/** A trigger named with {@code as label}, so that {@code triggeredBy(label)} can refer to it. */
public record TriggerLabel(String name, long guid) {
}
