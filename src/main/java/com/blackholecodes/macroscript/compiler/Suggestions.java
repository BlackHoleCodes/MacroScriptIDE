package com.blackholecodes.macroscript.compiler;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/** Finds the closest known name to a misspelled one, for "did you mean" messages. */
public final class Suggestions {

    private static final int MAX_DISTANCE = 3;

    private Suggestions() {
    }

    public static Optional<String> closest(String typed, Collection<String> candidates) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        String lowerTyped = typed.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            int distance = distance(lowerTyped, candidate.toLowerCase(Locale.ROOT));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        int limit = Math.min(MAX_DISTANCE, Math.max(1, typed.length() / 2));
        return bestDistance <= limit ? Optional.of(best) : Optional.empty();
    }

    /** A sentence such as " Did you mean 'setWifi'?" or an empty string. */
    public static String didYouMean(String typed, Collection<String> candidates) {
        return closest(typed, candidates).map(name -> " Did you mean '" + name + "'?").orElse("");
    }

    /** Levenshtein edit distance. */
    static int distance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}
