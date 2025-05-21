package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserLoginDto;
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

            InputStream is = exchange.getRequestBody();
            UserLoginDto dto = objectMapper.readValue(is, UserLoginDto.class);

            if (dto.getPhone() == null || dto.getPassword() == null) {
                sendResponse(exchange, 400, Message.MISSING_FIELDS.get());
                return;
            }

            User user = userService.findByPhone(dto.getPhone());

            if (user == null || !user.getPassword().equals(dto.getPassword())) {
                sendResponse(exchange, 401, Message.LOGIN_FAILED.get());
                return;
            }

            String token = TokenService.generateToken(
                    String.valueOf(user.getUserId()),
                    user.getRole().name()
            );

            String response = """
                {
                  "message": "%s",
                  "userId": "%s",
                  "token": "%s"
                }
                """.formatted(
                    Message.LOGIN_SUCCESS.get(),
                    user.getUserId(),
                    token
            );

            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
