package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.RatingRequestDto;
import com.foodApp.dto.RatingResponseDto;
import com.foodApp.dto.ItemRatingsSummaryDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.RatingService;
import com.foodApp.service.RatingServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.exception.MenuItemNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class RatingHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RatingService ratingService = new RatingServiceImpl();

    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        String token = extractToken(exchange);
        int userId = -1;
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
            userId = Integer.parseInt(jwt.getSubject());
        } catch (Exception e) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }

        if (path.equals("/ratings") && "POST".equalsIgnoreCase(method)) {
            handleSubmitRating(exchange, userId);
        } else if (path.matches("/ratings/items/\\d+") && "GET".equalsIgnoreCase(method)) {
            handleGetItemRatings(exchange, userId);
        } else if (path.matches("/ratings/\\d+")) {
            int ratingId = -1;
            try {
                ratingId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                handleGetRatingById(exchange, userId, ratingId);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdateRating(exchange, userId, ratingId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteRating(exchange, userId, ratingId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handleSubmitRating(HttpExchange exchange, int userId) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            RatingRequestDto requestDto = objectMapper.readValue(is, RatingRequestDto.class);
            RatingResponseDto responseDto = ratingService.submitRating(userId, requestDto);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (OrderNotFoundException | UserNotFoundException | MenuItemNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleGetItemRatings(HttpExchange exchange, int userId) throws IOException {
        String path = exchange.getRequestURI().getPath();
        int itemId;
        try {
            itemId = Integer.parseInt(path.split("/")[3]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }

        try {
            ItemRatingsSummaryDto summary = ratingService.getItemRatings(itemId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(summary));
        } catch (MenuItemNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleGetRatingById(HttpExchange exchange, int userId, int ratingId) throws IOException {
        try {
            RatingResponseDto responseDto = ratingService.getRatingById(ratingId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (OrderNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleUpdateRating(HttpExchange exchange, int userId, int ratingId) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            RatingRequestDto requestDto = objectMapper.readValue(is, RatingRequestDto.class);
            RatingResponseDto responseDto = ratingService.updateRating(ratingId, userId, requestDto);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (OrderNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleDeleteRating(HttpExchange exchange, int userId, int ratingId) throws IOException {
        try {
            ratingService.deleteRating(ratingId, userId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        } catch (OrderNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }
}