package com.blackholecodes.macroscript.decompiler;

/**
 * One MacroScript file made by the decompiler.
 *
 * @param fileName a file name made from the macro or action block name, ending in .ms
 * @param source   the MacroScript code
 */
public record DecompiledFile(String fileName, String source) {
}
