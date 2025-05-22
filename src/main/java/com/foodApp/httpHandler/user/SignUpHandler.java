package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserSignupDto;
import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.foodApp.httpHandler.BaseHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SignUpHandler extends BaseHandler implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }

            InputStream is = exchange.getRequestBody();
            UserSignupDto dto = objectMapper.readValue(is, UserSignupDto.class);

            if (dto.getPhone() == null || dto.getPassword() == null || dto.getFullName() == null
                    || dto.getRole() == null || dto.getAddress() == null || dto.getPhone().length() != 11) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }

            // Check if user with phone already exists
            if (userService.findByPhone(dto.getPhone()) != null) {
                sendResponse(exchange, 409, Message.PHONE_ALREADY_EXIST.get());
                return;
            }

            User user = new User();
            user.setName(dto.getFullName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            user.setAddress(dto.getAddress());
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));

            if (dto.getBankInfo() != null) {
                user.setBankName(dto.getBankInfo().getBankName());
                user.setAccountNumber(dto.getBankInfo().getAccountNumber());
            }

            userService.registerUser(user);

            String token = TokenService.generateToken(
                    String.valueOf(user.getUserId()), user.getRole().name());

            String response = """
                {
                  "message": "%s",
                  "userId": "%s",
                  "token": "%s"
                }
                """.formatted(
                    Message.SIGNUP_SUCCESS.get(),
                    String.valueOf(user.getUserId()),
                    token
            );

            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
