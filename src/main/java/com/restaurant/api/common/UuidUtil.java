package com.restaurant.api.common;

import java.util.UUID;

public class UuidUtil {

    private UuidUtil() {}

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
