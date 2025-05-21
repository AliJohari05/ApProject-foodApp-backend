package com.foodApp.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }

        if (value == null) {
            throw new RuntimeException("Environment variable '" + key + "' not found!");
        }

        return value;
    }
}
