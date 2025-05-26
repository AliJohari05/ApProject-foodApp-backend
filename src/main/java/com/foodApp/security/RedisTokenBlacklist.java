package com.foodApp.security;

import com.foodApp.config.RedisClient;

public class RedisTokenBlacklist {

    private static final String BLACKLIST_KEY = "jwt_blacklist";

    public static void add(String token, long expirationSeconds) {
        RedisClient.getInstance().setex(token, expirationSeconds, "blacklisted");
    }

    public static boolean isBlacklisted(String token) {
        return RedisClient.getInstance().exists(token);
    }
}
