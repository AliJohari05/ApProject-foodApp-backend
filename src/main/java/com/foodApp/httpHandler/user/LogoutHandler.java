package com.foodApp.httpHandler.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.security.RedisTokenBlacklist;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class LogoutHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, objectMapper.writeValueAsString(Message.METHOD_NOT_ALLOWED.get()));
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Message.UNAUTHORIZED.get()));
            return;
        }

        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            long now = System.currentTimeMillis() / 1000;
            long exp = jwt.getExpiresAt().getTime() / 1000;
            long ttl = exp - now;

            if (ttl > 0) {
                RedisTokenBlacklist.add(token, ttl);
            }

            sendResponse(exchange, 200, objectMapper.writeValueAsString(Message.LOGOUT_SUCCESS.get()));
        } catch (SecurityException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Message.FORBIDDEN.get()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Message.SERVER_ERROR.get()));
        }
    }
}

