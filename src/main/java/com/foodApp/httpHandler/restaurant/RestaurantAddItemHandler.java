package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;

import com.foodApp.dto.AddItemDto;
import com.foodApp.model.Role;
import com.foodApp.model.MenuItem;
import com.foodApp.security.TokenService;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RestaurantAddItemHandler extends BaseHandler implements HttpHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if(!exchange.getRequestMethod().equals("POST")){
                sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
                return;
            }
            DecodedJWT jwt;
            try {
                jwt = TokenService.verifyToken(token);
            } catch (Exception e) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            if (!jwt.getClaim("role").asString().equals(Role.SELLER.name())) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            Integer restaurantId = getRestaurantIdFromPath(exchange);
            if (restaurantId == null) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid `restaurantId` in path\"}");
                return;
            }
            AddItemDto addItemDto = objectMapper.readValue(exchange.getRequestBody(), AddItemDto.class);

            String userPhone = jwt.getSubject();

            MenuItem newMenuItem = restaurantService.addMenuItemToRestaurant(restaurantId, addItemDto, userPhone);

            String responseJson = objectMapper.writeValueAsString(newMenuItem);
            sendResponse(exchange, 201, responseJson);
        } catch (Exception e) {
            sendResponse(exchange,500, Message.SERVER_ERROR.get());
        }
    }
    private Integer getRestaurantIdFromPath(HttpExchange exchange){
    String path = exchange.getRequestURI().getPath();
    String[] segments = path.split("/");
    if(segments.length == 4 && segments[1].equals("restaurants") && segments[3].equals("item")){
        try {
            return Integer.parseInt(segments[2]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
        return null;
    }
}
