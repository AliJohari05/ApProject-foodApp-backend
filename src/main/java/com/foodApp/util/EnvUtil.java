package com.foodApp.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir")) // ریشه پروژه
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        String value = dotenv.get(key);
        System.out.println("🔍 EnvUtil.get(\"" + key + "\") = " + value);
        return value;
    }
}
