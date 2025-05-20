package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserSignupDto;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class signUpHandler extends BaseHandler implements HttpHandler {

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

            if (dto.getPhone() == null || dto.getPassword() == null || dto.getFullName() == null) {
                sendResponse(exchange, 400, Message.MISSING_FIELDS.get());
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

            String token= TokenService.generateToken(user.getUserId(),user.getRole().toString());

            sendJson(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Message.RESTAURANT_REGISTERED.get());
        }
    }

}
