package com.blackholecodes.macroscript.mapping;

/** How a command can be used from MacroScript code. */
public enum CommandStatus {
    /** Written as a normal call, for example {@code setWifi(TURN_ON)}. */
    CALLABLE,
    /** Produced only by language syntax (if, while, assignment...). It cannot be called. */
    SYNTAX,
    /** Callable, but MacroDroid no longer supports it well. The compiler gives a warning. */
    DEPRECATED,
    /** Callable. The compiler writes the class type of the command that it is an alias for. */
    ALIAS;

    static CommandStatus fromJson(String value) {
        return valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }

    public boolean isCallable() {
        return this != SYNTAX;
    }
}
