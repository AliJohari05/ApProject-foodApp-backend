package com.foodApp.httpHandler.restaurant;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.RestaurantDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestaurantGetHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper= new ObjectMapper();
    private final UserService userService= new UserServiceImpl();
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
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
            int ownerId = Integer.parseInt(jwt.getSubject());

            List<Restaurant> restaurants = restaurantService.getRestaurantByOwnerId(ownerId);

            List<RestaurantDto> restaurantDtos = new ArrayList<>();
            for (Restaurant restaurant : restaurants){
                restaurantDtos.add(new RestaurantDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getAddress(),
                        restaurant.getPhone(),
                        restaurant.getLogobase64(),
                        restaurant.getTaxFee(),
                        restaurant.getAdditionalFee()
                ));
            }
            sendResponse(exchange, 200, objectMapper.writeValueAsString(restaurantDtos));
        }catch (NumberFormatException e ){
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (Exception e) {
            sendResponse(exchange,500,Message.SERVER_ERROR.get());
        }
    }
}
