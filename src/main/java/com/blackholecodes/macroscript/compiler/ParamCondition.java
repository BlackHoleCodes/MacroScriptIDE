package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates the {@code requiredWhen} and {@code usedWhen} conditions of the mapping file,
 * for example {@code "m_option=1"}, {@code "m_btState=2 or m_btState=3"} or
 * {@code "m_useVariable && !useUri"}.
 *
 * <p>Some conditions in the mapping are written in words ("m_intRandom && integer type").
 * Those cannot be evaluated, and the result is then empty, so the compiler does not enforce
 * them.
 */
public final class ParamCondition {

    private static final Pattern OR = Pattern.compile("\\s*(\\|\\||\\bor\\b|,)\\s*");
    private static final Pattern AND = Pattern.compile("\\s*(&&|\\band\\b)\\s*");
    private static final Pattern COMPARISON = Pattern.compile("([A-Za-z_][\\w.]*)\\s*(!=|=)\\s*(\\S+)");
    private static final Pattern FLAG = Pattern.compile("(!?)\\s*([A-Za-z_][\\w.]*)");

    private ParamCondition() {
    }

    /**
     * @param condition the condition text from the mapping
     * @param command   the command, used to find the parameters the condition names
     * @param valueOf   gives the current JSON value of a parameter, or null when it has none
     * @return the result, or empty when the condition cannot be evaluated
     */
    public static Optional<Boolean> evaluate(String condition, CommandSpec command, Function<ParamSpec, JsonNode> valueOf) {
        if (condition == null || condition.isBlank()) {
            return Optional.empty();
        }
        boolean any = false;
        for (String alternative : OR.split(condition.trim())) {
            boolean all = true;
            for (String term : AND.split(alternative.trim())) {
                Optional<Boolean> result = evaluateTerm(term.trim(), command, valueOf);
                if (result.isEmpty()) {
                    return Optional.empty();
                }
                all &= result.get();
            }
            any |= all;
        }
        return Optional.of(any);
    }

    private static Optional<Boolean> evaluateTerm(String term, CommandSpec command, Function<ParamSpec, JsonNode> valueOf) {
        Matcher comparison = COMPARISON.matcher(term);
        if (comparison.matches()) {
            Optional<ParamSpec> param = find(command, comparison.group(1));
            if (param.isEmpty()) {
                return Optional.empty();
            }
            JsonNode actual = valueOf.apply(param.get());
            boolean equal = false;
            for (String expected : comparison.group(3).split("\\|")) {
                equal |= matches(param.get(), actual, expected);
            }
            return Optional.of(comparison.group(2).equals("=") == equal);
        }
        Matcher flag = FLAG.matcher(term);
        if (flag.matches()) {
            Optional<ParamSpec> param = find(command, flag.group(2));
            if (param.isEmpty()) {
                return Optional.empty();
            }
            JsonNode actual = valueOf.apply(param.get());
            boolean set = actual != null && !actual.isNull() && (!actual.isBoolean() || actual.booleanValue());
            return Optional.of(flag.group(1).isEmpty() == set);
        }
        return Optional.empty();
    }

    private static boolean matches(ParamSpec param, JsonNode actual, String expected) {
        if (expected.equals("null")) {
            return actual == null || actual.isNull();
        }
        if (actual == null || actual.isNull()) {
            return false;
        }
        Optional<ConstantSpec> constant = param.constant(expected);
        if (constant.isPresent()) {
            return constant.get().value().equals(actual);
        }
        if (actual.isBoolean()) {
            return expected.equals(Boolean.toString(actual.booleanValue()));
        }
        if (actual.isNumber()) {
            try {
                return Double.parseDouble(expected) == actual.doubleValue();
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return expected.equals(actual.asText());
    }

    /** Finds a parameter by MacroScript name or JSON field, with or without the m_ prefix. */
    private static Optional<ParamSpec> find(CommandSpec command, String name) {
        String bare = name.startsWith("m_") ? name.substring(2) : name;
        for (ParamSpec param : command.params()) {
            if (name.equals(param.jsonField()) || name.equals(param.name()) || bare.equals(param.name())
                    || ("m_" + bare).equals(param.jsonField())) {
                return Optional.of(param);
            }
        }
        return Optional.empty();
    }
}
