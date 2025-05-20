package com.foodApp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class TokenService {
private static final String SECRET = System.getenv("JWT_SECRET");
private static  final  Algorithm algo = Algorithm.HMAC256(SECRET);

private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000;

public static String generateToken(String userId,String role){
    Date now=new Date();
    Date expiresAt = new Date(now.getTime()+EXPIRATION_MS);
    return JWT.create()
            .withSubject(userId)
            .withClaim("role",role)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algo);
}
public static DecodedJWT verifyToken(String token){
    JWTVerifier verifier = JWT.require(algo).build();
    return verifier.verify(token);
}
}
