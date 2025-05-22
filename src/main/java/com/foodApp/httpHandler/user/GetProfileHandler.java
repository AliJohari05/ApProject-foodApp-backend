package com.foodApp.httpHandler.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.User;
import com.foodApp.security.AuthHelper;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GetProfileHandler extends BaseHandler implements HttpHandler {

    private final UserService userService = new UserServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            DecodedJWT jwt = AuthHelper.verifyTokenFromHeader(authHeader);

            int userId = Integer.parseInt(jwt.getSubject());
            User user = userService.findById(userId);

            if (user == null) {
                sendResponse(exchange, 404, "User not found");
                return;
            }

            String json = objectMapper.writeValueAsString(user.toDto());
            sendResponse(exchange, 200, json);

        } catch (Exception e) {
            sendResponse(exchange, 401, "Unauthorized: " + e.getMessage());
        }
    }
}
