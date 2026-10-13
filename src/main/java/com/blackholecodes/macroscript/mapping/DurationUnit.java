package com.blackholecodes.macroscript.mapping;

/** The unit that a duration parameter stores its value in. */
public enum DurationUnit {
    MILLISECONDS("ms", 1L),
    SECONDS("s", 1_000L),
    MINUTES("m", 60_000L),
    HOURS("h", 3_600_000L);

    private final String suffix;
    private final long millis;

    DurationUnit(String suffix, long millis) {
        this.suffix = suffix;
        this.millis = millis;
    }

    /** The MacroScript suffix for this unit, for example "s" in {@code 30s}. */
    public String suffix() {
        return suffix;
    }

    /** The number of milliseconds in one of this unit. */
    public long millis() {
        return millis;
    }

    static DurationUnit fromJson(String value) {
        return switch (value) {
            case "ms" -> MILLISECONDS;
            case "s" -> SECONDS;
            case "min", "m" -> MINUTES;
            case "h" -> HOURS;
            default -> throw new IllegalArgumentException("Unknown duration unit: " + value);
        };
    }
}
