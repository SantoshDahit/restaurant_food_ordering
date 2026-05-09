package com.restaurant.api.common;

import java.security.SecureRandom;

public class RestaurantCodeGenerator {

    // Excludes 0/O/1/I/L to avoid visual ambiguity in printed/spoken codes.
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private RestaurantCodeGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
