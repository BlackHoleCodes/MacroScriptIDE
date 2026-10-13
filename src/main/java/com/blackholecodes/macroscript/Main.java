package com.blackholecodes.macroscript;

import com.blackholecodes.macroscript.cli.CommandLine;
import com.blackholecodes.macroscript.ide.IdeLauncher;

/**
 * Starts the program. With {@code compile} or {@code decompile} as the first argument it
 * runs on the command line; otherwise it opens the IDE.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (CommandLine.handles(args)) {
            System.exit(new CommandLine(System.out, System.err).run(args));
        }
        IdeLauncher.launch(args);
    }
}
