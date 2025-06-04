package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.foodApp.httpHandler.BaseHandler;

import com.foodApp.model.Role;
import com.foodApp.model.Restaurant;
import com.foodApp.model.User;

import com.foodApp.exception.*;

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
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import java.util.Map;
import java.util.HashMap;
public class RestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        if(exchange.getRequestMethod().equalsIgnoreCase("POST") && exchange.getRequestURI().getPath().equalsIgnoreCase("/restaurants")){
            handlePostRestaurant(exchange);
        } else if (exchange.getRequestURI().getPath().equalsIgnoreCase("/restaurants/\\d+")) {
            handlePutRestaurant(exchange);
        }
        else {
            sendResponse(exchange, 404, Message.ERROR_404.get());
        }
    }
    private void handlePostRestaurant(HttpExchange exchange) throws IOException {
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
            Restaurant restaurantModel = new Restaurant();
            restaurantModel.setName(restaurantDto.getName());
            restaurantModel.setAddress(restaurantDto.getAddress());
            restaurantModel.setPhone(restaurantDto.getPhone());
            restaurantModel.setLogobase64(restaurantDto.getLogoBase64());
            restaurantModel.setTaxFee(restaurantDto.getTax_fee() != null ? restaurantDto.getTax_fee() : 0);
            restaurantModel.setAdditionalFee(restaurantDto.getAdditional_fee() != null ? restaurantDto.getAdditional_fee() : 0);

            int ownerId;
            ownerId = Integer.parseInt(jwt.getSubject());

            User owner = userService.findById(ownerId);
            restaurantModel.setOwner(owner);
            restaurantModel.setApproved(false);
            restaurantModel.setCreatedAt(java.time.LocalDateTime.now());
            restaurantModel.setUpdatedAt(java.time.LocalDateTime.now());


            Restaurant savedRestaurant = restaurantService.registerRestaurantAndReturn(restaurantModel);


            RestaurantDto responseDto = new RestaurantDto();
            responseDto.setId(savedRestaurant.getId());
            responseDto.setName(savedRestaurant.getName());
            responseDto.setAddress(savedRestaurant.getAddress());
            responseDto.setPhone(savedRestaurant.getPhone());
            responseDto.setLogoBase64(savedRestaurant.getLogobase64());
            responseDto.setTax_fee(savedRestaurant.getTaxFee());
            responseDto.setAdditional_fee(savedRestaurant.getAdditionalFee());

            sendResponse(exchange, 201, objectMapper.writeValueAsString(responseDto));

        }catch (Exception e){
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + Message.SERVER_ERROR.get() + ": " + e.getMessage() + "\"}");
        }
    }

    private void handlePutRestaurant(HttpExchange exchange) throws IOException {
        try {
            // 1. استخراج ID رستوران از مسیر
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            if (segments.length < 3) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }
            int restaurantId;
            try {
                restaurantId = Integer.parseInt(segments[2]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
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
                sendResponse(exchange, 401, Message.UNAUTHORIZED.get()); // یا 403
                return;
            }

            String userRole = jwt.getClaim("role").asString();
            if (!Role.SELLER.name().equalsIgnoreCase(userRole)) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            int ownerIdFromToken = Integer.parseInt(jwt.getSubject());

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
            }
            String validationError = restaurantDto.validateFields(); //
            if (validationError != null) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }
            Restaurant updatedRestaurant = restaurantService.updateRestaurantForPut(restaurantId, restaurantDto, ownerIdFromToken);
            RestaurantDto responseDto = new RestaurantDto(
                    updatedRestaurant.getId(),
                    updatedRestaurant.getName(),
                    updatedRestaurant.getAddress(),
                    updatedRestaurant.getPhone(),
                    updatedRestaurant.getLogobase64(),
                    updatedRestaurant.getTaxFee(),
                    updatedRestaurant.getAdditionalFee()
            );
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (RestaurantNotFoundException e) {
            sendResponse(exchange, 404, Message.ERROR_404.get());
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, Message.UNAUTHORIZED.get());
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + Message.SERVER_ERROR.get() + ": " + e.getMessage() + "\"}");
        }
    }
}
