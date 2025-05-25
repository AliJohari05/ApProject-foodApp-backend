package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserSignupDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

            if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
                sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
                return;
            }

            InputStream is = exchange.getRequestBody();
            UserSignupDto dto = objectMapper.readValue(is, UserSignupDto.class);

            if (dto.getPhone() == null || dto.getPassword() == null || dto.getFullName() == null || dto.getRole() == null || dto.getAddress() == null) {
                sendResponse(exchange, 400, Message.MISSING_FIELDS.get());
                return;
            }

            String validationError = dto.validateFields();
            if (validationError != null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", validationError);
                String errorJson = objectMapper.writeValueAsString(errorMap);
                sendResponse(exchange, 400, errorJson);
                return;
            }
            if(!dto.isValid()){
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
            }

            if (userService.phoneExists(dto.getPhone())) {
                sendResponse(exchange, 409, Message.PHONE_ALREADY_EXIST.get());
                return;
            }

            User user = UserSignupDto.toUser(dto);
            User savedUser = userService.registerUser(user);


            String token = TokenService.generateToken(String.valueOf(savedUser.getUserId()), savedUser.getRole().name());

            Map<String, Object> result = new HashMap<>();
            result.put("message", Message.SIGNUP_SUCCESS.get());
            result.put("user_id", savedUser.getUserId());
            result.put("token", token);

            String responseJson = objectMapper.writeValueAsString(result);
            sendResponse(exchange, 200, responseJson);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
