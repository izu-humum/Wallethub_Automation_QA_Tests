package com.wallethub.qa.util;

import java.security.SecureRandom;

/**
 * Generates the random review text. Kept tiny and dependency-free.
 */
public final class RandomText {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String WORDS =
            "the quick brown fox jumps over a lazy dog while testing this insurance "
                    + "review flow with plenty of filler words to comfortably exceed the "
                    + "required minimum length for the submission";
    private static final String[] LEXICON = WORDS.split(" ");

    private RandomText() {
        // Utility class - not instantiable.
    }

    /**
     * Builds a sentence-like string of at least {@code minLength} characters by
     * appending random words. The first letter is capitalised and it ends with a
     * period, so it reads like a plausible review rather than gibberish.
     *
     * @param minLength minimum number of characters
     * @return generated text (never shorter than {@code minLength})
     */
    public static String ofAtLeast(int minLength) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < minLength) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(LEXICON[RANDOM.nextInt(LEXICON.length)]);
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.append('.').toString();
    }
}
