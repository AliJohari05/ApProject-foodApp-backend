package com.foodApp.httpHandler.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserLoginDto;
import com.foodApp.model.User;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class loginHandler implements HttpHandler {
    private static final UserService userService = new UserServiceImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
            InputStream requestBody = exchange.getRequestBody();
            UserLoginDto loginRequest = objectMapper.readValue(requestBody, UserLoginDto.class);
            if (loginRequest.getPhone() == null || loginRequest.getPassword() == null) {
                sendResponse(exchange, 400, "Missing phone or password");
            }
            User user = userService.login(loginRequest.getPhone(), loginRequest.getPassword());
            String responseJson = objectMapper.writeValueAsString(user);
            sendResponse(exchange, 200, responseJson);

        }catch (Exception e) {
            sendResponse(exchange, 401, "Login failed: " + e.getMessage());

        }

    }
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
