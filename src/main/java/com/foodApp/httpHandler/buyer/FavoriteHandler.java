package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodApp.exception.DatabaseException;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.exception.UserNotFoundException;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.FavoriteService;
import com.foodApp.service.FavoriteServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavoriteHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FavoriteService favoriteService = new FavoriteServiceImpl();
    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = extractToken(exchange);
        int userId;
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.CUSTOMER.name(), Role.ADMIN.name()); // خریداران و ادمین ها مجاز به مدیریت favorites
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            userId = Integer.parseInt(jwt.getSubject());
        } catch (Exception e) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if(path.equals("/favorite")) {
            if(method.equals("GET")) {
                GetHandel(exchange, userId);
            }
            else{
                sendResponse(exchange,405,Message.METHOD_NOT_ALLOWED.get());
                return;
            }
        }
        else if(path.matches("/favorite/\\d+")) {
            int restaurantId;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }
            if(method.equals("PUT")) {
                PutHandel(exchange,userId,restaurantId);
            }else if(method.equals("DELETE")) {
                DeleteHandel(exchange,userId,restaurantId);
            }
            else{
                sendResponse(exchange,405,Message.METHOD_NOT_ALLOWED.get());
                return;
            }

        }else{
            sendResponse(exchange,404, Message.ERROR_404.get());
            return;
        }


    }
    private void GetHandel(HttpExchange exchange , int userId) throws IOException {
        try {
            List<Restaurant> favoriteRestaurants = favoriteService.getUserFavoriteRestaurants(userId);
            String jsonResponse = objectMapper.writeValueAsString(favoriteRestaurants);
            sendResponse(exchange, 200, jsonResponse);
        } catch (UserNotFoundException e) {
            sendResponse(exchange, 404, Message.USER_NOT_FOUND.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }

    }
    private void PutHandel(HttpExchange exchange ,int userId,int restaurantId) throws IOException {
        try {
            favoriteService.addFavoriteRestaurant(userId, restaurantId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 409, objectMapper.writeValueAsString(Map.of("error", Message.CONFLICT.get()))); // استفاده از پیام CONFLICT
        } catch (UserNotFoundException | RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (DatabaseException e) {
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get())));
        }
    }
    private void DeleteHandel(HttpExchange exchange,int userId,int restaurantId) throws IOException {
        try {
            favoriteService.removeFavoriteRestaurant(userId, restaurantId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        } catch (UserNotFoundException | RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (DatabaseException e) {
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get())));
        }
    }
}
