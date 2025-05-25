package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.util.Message;
import com.foodApp.security.TokenService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RegisterRestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
                sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }
            InputStream body = exchange.getRequestBody();
            Restaurant restaurant = objectMapper.readValue(body, Restaurant.class);
            if(restaurant.getName() == null || restaurant.getAddress() == null || restaurant.getPhone() == null){
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }
            String token = extractToken(exchange);
            if(token == null){
                sendResponse(exchange,401,Message.UNAUTHORIZED.get());
                return;
            }
            DecodedJWT jwt;
            try {
                jwt =TokenService.verifyToken(token);
            }
            catch (Exception e){
                sendResponse(exchange,401,Message.UNAUTHORIZED.get());
                return;
            }
            String userRole = jwt.getClaim("role").asString();
            if(!Role.SELLER.name().equalsIgnoreCase(userRole)){
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            restaurantService.registerRestaurant(restaurant);
            sendResponse(exchange, 201, Message.RESTAURANT_REGISTERED.get());


        }catch (Exception e){
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

}
