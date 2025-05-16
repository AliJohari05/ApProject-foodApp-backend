package com.foodApp.httpHandler.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.model.User;
import com.foodApp.dto.UserSignupDto;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
public class signUpHandler implements HttpHandler {
    private static final UserService userService = new UserServiceImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
            InputStream requestBody = exchange.getRequestBody();
            UserSignupDto signupRequest = objectMapper.readValue(requestBody, UserSignupDto.class);

            if (signupRequest.getPhone() == null || signupRequest.getPassword() == null || signupRequest.getName() == null) {
                sendResponse(exchange, 400, "Missing required fields");
            }
            User user = new User();
            user.setName(signupRequest.getName());
            user.setPhone(signupRequest.getPhone());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(signupRequest.getPassword());
            user.setRole(signupRequest.getRole());
            user.setAddress(signupRequest.getAddress());
            userService.registerUser(user);
            sendResponse(exchange, 200, "User registered successfully");
        }catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
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
