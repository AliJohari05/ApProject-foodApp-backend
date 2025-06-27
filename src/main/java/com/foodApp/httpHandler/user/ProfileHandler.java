package com.foodApp.httpHandler.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserProfileDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.foodApp.util.ImageUploader; // Import ImageUploader
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileHandler extends BaseHandler implements HttpHandler {
    private final UserService userService = new UserServiceImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, jsonError(Message.METHOD_NOT_ALLOWED.get()));
            return;
        }

        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, jsonError(Message.UNAUTHORIZED.get()));
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
        } catch (Exception e) {
            sendResponse(exchange, 403, jsonError(Message.FORBIDDEN.get()));
            return;
        }

        int userId = Integer.parseInt(jwt.getSubject());
        if ("GET".equalsIgnoreCase(method)) {
            handleGet(exchange, userId);
        } else { // PUT method
            handlePut(exchange, userId);
        }
    }

    private void handleGet(HttpExchange exchange, int userId) throws IOException {
        User user = userService.findById(userId);
        if (user == null) {
            sendResponse(exchange, 404, jsonError(Message.USER_NOT_FOUND.get()));
            return;
        }
        UserProfileDto dto = new UserProfileDto(user);
        sendResponse(exchange, 200, objectMapper.writeValueAsString(dto));
    }

    private void handlePut(HttpExchange exchange, int userId) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, jsonError(Message.UNSUPPORTED_MEDIA_TYPE.get()));
            return;
        }

        try {
            InputStream is = exchange.getRequestBody();
            UserProfileDto dto = objectMapper.readValue(is, UserProfileDto.class);

            User existingUser = userService.findById(userId);
            if (existingUser == null) {
                sendResponse(exchange, 404, jsonError(Message.USER_NOT_FOUND.get()));
                return;
            }

            // --- Handle Profile Image Base64 Upload ---
            if (dto.getProfileImageBase64() != null) { // Check if image data is provided (can be empty string to clear)
                if (dto.getProfileImageBase64().isEmpty()) {
                    // User wants to clear the profile image
                    if (existingUser.getProfileImageUrl() != null) {
                        ImageUploader.deleteProfileImage(existingUser.getProfileImageUrl()); // Delete old image
                    }
                    existingUser.setProfileImageUrl(null); // Set to null in DB
                } else {
                    // User wants to upload a new image
                    String newImageUrl = ImageUploader.saveProfileImage(dto.getProfileImageBase64(), userId);
                    if (newImageUrl != null) {
                        if (existingUser.getProfileImageUrl() != null) {
                            ImageUploader.deleteProfileImage(existingUser.getProfileImageUrl()); // Delete old image
                        }
                        existingUser.setProfileImageUrl(newImageUrl);
                    } else {
                        sendResponse(exchange, 500, jsonError("Failed to process profile image."));
                        return;
                    }
                }
            }
            // --- End Handle Profile Image Base64 Upload ---


            boolean phoneChanged = dto.getPhone() != null && !dto.getPhone().equals(existingUser.getPhone());

            // Update other fields from DTO (only if provided in DTO)
            if(dto.getFullName() != null) existingUser.setName(dto.getFullName());
            if(dto.getPhone() != null) existingUser.setPhone(dto.getPhone());
            if(dto.getEmail() != null) existingUser.setEmail(dto.getEmail());
            if(dto.getAddress() != null) existingUser.setAddress(dto.getAddress());
            // profileImageUrl is handled above based on base64, or if explicitly null in DTO to clear.

            if (dto.getBankInfo() != null) {
                existingUser.setBankName(dto.getBankInfo().getBankName());
                existingUser.setAccountNumber(dto.getBankInfo().getAccountNumber());
            }

            userService.updateUser(existingUser); // This saves the updated user object

            if (phoneChanged) {
                String newToken = TokenService.generateToken(
                        String.valueOf(existingUser.getUserId()),
                        existingUser.getRole().name()
                );

                String json = """
                {
                  "message": "%s",
                  "newToken": "%s"
                }
                """.formatted(Message.PROFILE_UPDATED.get(), newToken);

                sendResponse(exchange, 200, json);
            } else {
                sendResponse(exchange, 200, """
                {
                  "message": "%s"
                }
                """.formatted(Message.PROFILE_UPDATED.get()));
            }

        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            sendResponse(exchange, 400, jsonError(Message.INVALID_INPUT.get()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, jsonError(Message.SERVER_ERROR.get()));
        }
    }

    private String jsonError(String message) {
        return """
        {
          "error": "%s"
        }
        """.formatted(message);
    }
}