package com.blackholecodes.macroscript.compiler;

import java.util.Optional;

/** Finds the source of an action block by its {@code @name}. */
public interface ActionBlockResolver {

    /** A resolver that finds nothing, for compiling a file on its own. */
    ActionBlockResolver NONE = name -> Optional.empty();

    /** The {@code .ms} file with {@code @actionBlock} and {@code @name(name)}, if there is one. */
    Optional<SourceUnit> find(String name);
}
