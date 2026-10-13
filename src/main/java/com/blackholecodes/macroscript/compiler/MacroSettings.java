package com.blackholecodes.macroscript.compiler;

/**
 * The macro settings that the file annotations give.
 *
 * @param headingColor the heading color as a signed ARGB int, or null
 * @param logLevel     0 for FULL, 1 for TRIGGERS_ONLY
 */
record MacroSettings(String name, String description, String category, boolean enabled, boolean favourite,
                     Integer headingColor, boolean noLog, int logLevel, boolean actionBlock) {
}
