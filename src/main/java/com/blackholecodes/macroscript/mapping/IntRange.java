package com.blackholecodes.macroscript.mapping;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The permitted range of an int parameter, for example 0-23 for an hour. */
public record IntRange(long min, long max) {

    private static final Pattern RANGE = Pattern.compile("(-?\\d+)\\s*-\\s*(-?\\d+)");

    public boolean contains(long value) {
        return value >= min && value <= max;
    }

    /** Reads a range such as {@code "0-23"}. Text in another form gives an empty result. */
    static Optional<IntRange> parse(String text) {
        Matcher matcher = RANGE.matcher(text.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new IntRange(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2))));
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }
}
