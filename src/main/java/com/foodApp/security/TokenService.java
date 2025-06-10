package com.foodApp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.foodApp.util.EnvUtil;

import java.util.Date;

public class TokenService {
    private static final String SECRET = EnvUtil.get("JWT_SECRET");
    static {
        System.out.println("JWT_SECRET from EnvUtil: " + SECRET);
    }
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    public static String generateToken(String userId, String role) {
        try {
            System.out.println("🌀 Generating token for userId=" + userId + ", role=" + role);

            Date now = new Date();
            Date expiresAt = new Date(now.getTime() + EXPIRATION_MS);

            return JWT.create()
                    .withSubject(userId)
                    .withClaim("role", role)
                    .withIssuedAt(now)
                    .withExpiresAt(expiresAt)
                    .sign(ALGORITHM);
        } catch (Exception e) {
            System.err.println("Token generation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public static DecodedJWT verifyToken(String token) {
        if (RedisTokenBlacklist.isBlacklisted(token)) {
            throw new SecurityException("Token is blacklisted");
        }

        return JWT.require(ALGORITHM).build().verify(token);
    }

}
