package com.foodApp.config;

import redis.clients.jedis.Jedis;

public class RedisClient {
    private static final Jedis jedis = new Jedis("localhost", 6379);

    public static Jedis getInstance() {
        return jedis;
    }
}
