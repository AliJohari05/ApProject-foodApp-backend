package com.foodApp.security;

import com.foodApp.config.RedisClient;
import redis.clients.jedis.Jedis;

public class RedisTokenBlacklist {

    private static final String BLACKLIST_KEY = "jwt_blacklist";

    public static void add(String token, long expirationSeconds) {
        RedisClient.getInstance().setex(token, expirationSeconds, "blacklisted");
    }


    public static boolean isBlacklisted(String token) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            return jedis.exists(token);
        } catch (Exception e) {
            System.err.println("Redis connection failed: " + e.getMessage());
            return false;
        }
    }
}
