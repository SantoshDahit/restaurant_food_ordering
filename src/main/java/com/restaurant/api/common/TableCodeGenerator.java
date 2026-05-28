package com.restaurant.api.common;

import java.security.SecureRandom;

/**
 * Generates short, human-friendly table codes like "BHO-7K2N":
 *   - up-to-3-letter prefix taken from the first letters of the restaurant
 *     name (spaces and punctuation stripped — matches the V8 SQL backfill)
 *   - dash
 *   - 4 random chars from an ambiguity-safe alphabet (no 0/O/1/I/L)
 *
 * Used as the shareable code on QR posters and the Launch → Table Ordering input.
 */
public class TableCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int SUFFIX_LENGTH = 4;
    private static final int PREFIX_LENGTH = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TableCodeGenerator() {}

    public static String generate(String restaurantName) {
        String prefix = prefix(restaurantName);
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return prefix + "-" + suffix;
    }

    private static String prefix(String name) {
        if (name == null || name.isBlank()) return "T";
        StringBuilder sb = new StringBuilder(PREFIX_LENGTH);
        for (int i = 0; i < name.length() && sb.length() < PREFIX_LENGTH; i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) sb.append(Character.toUpperCase(c));
        }
        return sb.length() == 0 ? "T" : sb.toString();
    }
}
