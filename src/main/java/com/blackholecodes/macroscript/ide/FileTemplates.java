package com.blackholecodes.macroscript.ide;

/** The text of new files. */
final class FileTemplates {

    private FileTemplates() {
    }

    static String macro(String name) {
        return """
                @name("%s")
                @description("What the macro does")

                variables {
                }

                triggers {
                    manual();
                }

                actions {
                    toast("Hello from %s");
                }
                """.formatted(escape(name), escape(name));
    }

    static String actionBlock(String name) {
        return """
                @actionBlock
                @name("%s")
                @description("What the action block does")

                variables {
                    in string message = "Hello";
                    out bool done;
                }

                actions {
                    toast(message);
                    done = true;
                }
                """.formatted(escape(name));
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$");
    }
}
