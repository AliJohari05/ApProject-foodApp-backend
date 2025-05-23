package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserLoginDto;
import com.foodApp.dto.UserProfileDto;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.foodApp.httpHandler.BaseHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler extends BaseHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }
            if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
                sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
                return;
            }

            InputStream is = exchange.getRequestBody();
            UserLoginDto dto = objectMapper.readValue(is, UserLoginDto.class);

            User user = userService.login(dto.getPhone(), dto.getPassword());
            if (user == null) {
                sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
                return;
            }

            String token = TokenService.generateToken(String.valueOf(user.getUserId()), user.getRole().name());
            UserProfileDto userDto = new UserProfileDto(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", Message.LOGIN_SUCCESS.get());
            result.put("token", token);
            result.put("user", userDto);

            sendResponse(exchange, 200, objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
