package com.blackholecodes.macroscript.decompiler;

import java.util.List;

/**
 * The result of decompiling one MacroDroid export.
 *
 * @param files    the macro first, then one file for each exported action block
 * @param warnings things the user should check, such as actions written as raw(...)
 */
public record DecompileResult(List<DecompiledFile> files, List<String> warnings) {

    /** The file of the macro itself. */
    public DecompiledFile main() {
        return files.get(0);
    }
}
