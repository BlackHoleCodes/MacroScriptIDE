package com.blackholecodes.macroscript;

import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes a MacroScript call with a sample value for each required argument of a command.
 * The tests use it to run every command of the mapping through the compiler.
 */
public final class SampleArguments {

    /** The variables block that the sample values refer to. */
    public static final String VARIABLES = """
            variables {
                string text;
                int number;
                bool flag;
                dict data;
            }
            """;

    private SampleArguments() {
    }

    public static String call(CommandSpec command) {
        List<String> arguments = new ArrayList<>();
        for (ParamSpec param : command.params()) {
            if (param.required() && param.isWrittenByUser()) {
                arguments.add(param.name() + ": " + sample(param));
            }
        }
        return command.name() + "(" + String.join(", ", arguments) + ")";
    }

    /** A source file that uses the command once, in the block of its category. */
    public static String file(CommandSpec command) {
        String call = call(command);
        return switch (command.category()) {
            case TRIGGER -> "@description(\"test\")\n" + VARIABLES + "triggers { " + call + "; }\nactions { log(\"x\"); }\n";
            case CONSTRAINT -> "@description(\"test\")\n" + VARIABLES + "triggers { manual(); }\nconstraints { " + call
                    + "; }\nactions { log(\"x\"); }\n";
            case ACTION -> "@description(\"test\")\n" + VARIABLES + "triggers { manual(); }\nactions { " + call + "; }\n";
        };
    }

    static String sample(ParamSpec param) {
        return switch (param.kind()) {
            case BOOL -> "true";
            case INT -> param.range() != null ? Long.toString(param.range().min()) : "1";
            case DECIMAL -> "1.5";
            case STRING, APP_NAME -> "\"sample\"";
            case TEXT -> "\"Hello ${text}\"";
            case ENUM -> param.constants().get(0).name();
            case FLAGS -> "[" + param.constants().get(0).name().replaceFirst("^_", "") + "]";
            case DURATION -> param.unit() == null ? "5s" : "5" + param.unit().suffix();
            case COLOR -> "#FF112233";
            case COLOR_LIST -> "[#FF112233, #FF445566]";
            case VARIABLE -> "text";
            case VARIABLE_LIST -> "[text]";
            case DICT_PATH -> "[\"key\"]";
            case APP -> "app(\"com.example\", \"Example\")";
            case APP_LIST -> "[app(\"com.example\", \"Example\")]";
            case CONTACT -> "contact(\"Mom\", \"+15551234567\")";
            case CONTACT_LIST -> "[contact(\"Mom\")]";
            case LIST -> "[\"a\", \"b\"]";
            case OBJECT, RAW_OBJECT -> "{\"key\": \"value\"}";
            case OBJECT_LIST -> "[{\"name\": \"a\"}]";
            case HTTP_PARAMS -> "{\"Accept\": \"text/plain\"}";
            case BLOCK_REF -> "block(\"Sample Block\")";
            case BLOCK_REF_LIST -> "[block(\"Sample Block\"), null]";
            case TRIGGER_LIST -> "shakeDevice()";
            case CONDITION -> "flag";
            case STREAM_MAP -> "{MUSIC: 50}";
            case FIXED -> throw new IllegalStateException("Fixed parameters are not written");
        };
    }

    /** The action block that block("Sample Block") refers to. */
    public static final String SAMPLE_BLOCK = """
            @actionBlock
            @name("Sample Block")
            @description("test")
            actions { log("block"); }
            """;

    public static boolean isCallable(CommandSpec command) {
        return command.status().isCallable() && command.category() != null
                && !(command.category() == CommandCategory.ACTION && command.name().equals("actionBlock"));
    }
}
