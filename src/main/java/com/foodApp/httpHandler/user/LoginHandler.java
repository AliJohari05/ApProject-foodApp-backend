package com.foodApp.httpHandler.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserLoginDto;
import com.foodApp.dto.UserProfileDto;
import com.foodApp.exception.InvalidPasswordException; // Import InvalidPasswordException
import com.foodApp.exception.UserNotFoundException;   // Import UserNotFoundException
import com.foodApp.model.Role;
import com.foodApp.model.Status;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.foodApp.httpHandler.BaseHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler extends BaseHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws JsonProcessingException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
                return;
            }
            if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
                sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
                return;
            }

            InputStream is = exchange.getRequestBody();
            UserLoginDto dto = objectMapper.readValue(is, UserLoginDto.class);

            User user = userService.login(dto.getPhone(), dto.getPassword());
            System.out.println("User returned: " + user);

            // This block for user == null is technically redundant if userService.login throws UserNotFoundException
            // but keeping it for safety if logic slightly changes or for non-exception path
            if (user == null) {
                System.out.println("User not found or password incorrect (should be handled by exception)");
                sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
                return;
            }
            // Check status for SELLER and COURIER roles
            if (user.getRole() == Role.SELLER || user.getRole() == Role.COURIER) {
                if (user.getStatus() != Status.APPROVED) {
                    // FIX: Specific message for PENDING/REJECTED status
                    String specificErrorMessage = "Your account status is: " + user.getStatus().name() + ". Please wait for admin approval.";
                    sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", specificErrorMessage)));
                    return;
                }
            }
            String token = TokenService.generateToken(String.valueOf(user.getUserId()), user.getRole().name());
            System.out.println("Token generation passed");

            UserProfileDto userDto = new UserProfileDto(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", Message.LOGIN_SUCCESS.get());
            result.put("token", token);
            result.put("user", userDto);

            System.out.println("Login successful. Sending response...");

            sendResponse(exchange, 200, objectMapper.writeValueAsString(result));
        } catch (UserNotFoundException e) { // FIX: Catch specific exception for user not found
            System.err.println("User not found: " + e.getMessage());
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get()))); // Unauthorized (common for user not found/invalid credentials)
        } catch (InvalidPasswordException e) { // FIX: Catch specific exception for invalid password
            System.err.println("Invalid password: " + e.getMessage());
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get()))); // Unauthorized
        } catch (Exception e) { // Catch all other unexpected exceptions
            System.err.println("UNEXPECTED EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get()))); // Ensure 500 is always JSON
        }
    }
}