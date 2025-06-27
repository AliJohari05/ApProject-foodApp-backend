package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.FavoriteService;
import com.foodApp.service.FavoriteServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.exception.DatabaseException;


import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class FavoriteHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FavoriteService favoriteService = new FavoriteServiceImpl();

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

        if (path.equals("/favorites") && "GET".equalsIgnoreCase(method)) {
            handleGetFavorites(exchange, userId);
        } else if (path.matches("/favorites/\\d+")) {
            int restaurantId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                handleAddFavorite(exchange, userId, restaurantId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleRemoveFavorite(exchange, userId, restaurantId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handleGetFavorites(HttpExchange exchange, int userId) throws IOException {
        try {
            List<Restaurant> favoriteRestaurants = favoriteService.getUserFavoriteRestaurants(userId);
            String jsonResponse = objectMapper.writeValueAsString(favoriteRestaurants);
            sendResponse(exchange, 200, jsonResponse);
        } catch (UserNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.USER_NOT_FOUND.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleAddFavorite(HttpExchange exchange, int userId, int restaurantId) throws IOException {
        try {
            favoriteService.addFavoriteRestaurant(userId, restaurantId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 409, objectMapper.writeValueAsString(Map.of("error", Message.CONFLICT.get())));
        } catch (UserNotFoundException | RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (DatabaseException e) {
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleRemoveFavorite(HttpExchange exchange, int userId, int restaurantId) throws IOException {
        try {
            favoriteService.removeFavoriteRestaurant(userId, restaurantId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        } catch (UserNotFoundException | RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (DatabaseException e) {
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }
}