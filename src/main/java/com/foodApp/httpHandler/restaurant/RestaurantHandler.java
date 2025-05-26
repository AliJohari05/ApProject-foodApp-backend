package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.foodApp.httpHandler.BaseHandler;

import com.foodApp.model.Role;

import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;

import com.foodApp.util.Message;

import com.foodApp.security.TokenService;

import com.foodApp.dto.RestaurantDto;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;

public class RestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
                sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }
            InputStream body = exchange.getRequestBody();
            RestaurantDto restaurantDto;
            try {
                restaurantDto = objectMapper.readValue(body, RestaurantDto.class);
            } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            } catch (Exception e) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }

            if (!restaurantDto.hasRequiredFields()) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }

            String validationError = restaurantDto.validateFields();
            if (validationError != null) {
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
