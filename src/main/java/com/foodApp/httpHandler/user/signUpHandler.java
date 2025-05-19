package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserSignupDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.User;
import com.foodApp.model.Role;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;

public class signUpHandler extends BaseHandler implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            InputStream is = exchange.getRequestBody();
            UserSignupDto dto = objectMapper.readValue(is, UserSignupDto.class);

            if (dto.getPhone() == null || dto.getPassword() == null || dto.getFullName() == null) {
                sendResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
                return;
            }
            User user = new User();
            user.setName(dto.getFullName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            user.setAddress(dto.getAddress());
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));

            userService.registerUser(user);

            String response = """
                {
                  "message": "User registered successfully",
                  "user": {
                    "name": "%s",
                    "phone": "%s",
                    "role": "%s"
                  }
                }
                """.formatted(user.getName(), user.getPhone(), user.getRole());

            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }
}

