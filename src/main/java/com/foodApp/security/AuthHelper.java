package com.foodApp.security;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Set;

public class AuthHelper {

    public static DecodedJWT verifyTokenFromHeader(String authHeader) throws RuntimeException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid token");
        }
        String token = authHeader.substring("Bearer ".length());
        return TokenService.verifyToken(token);
    }

    public static void checkRole(DecodedJWT jwt, Set<String> allowedRoles) {
        String role = jwt.getClaim("role").asString();
        if (!allowedRoles.contains(role)) {
            throw new RuntimeException("Access Denied: role not allowed");
        }
    }
}
