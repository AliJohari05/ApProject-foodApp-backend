package com.foodApp.httpHandler.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.security.RedisTokenBlacklist;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class LogoutHandler extends BaseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
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

            sendResponse(exchange, 200, Message.LOGOUT_SUCCESS.get());
        } catch (SecurityException e) {
            sendResponse(exchange, 403, Message.FORBIDDEN.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}

