package com.foodApp.httpHandler.restaurant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
public class GetRestaurantsByOwnerHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(!exchange.getRequestMethod().equals("GET")){
                sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            }
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length != 5) {
                sendResponse(exchange, 400, "Invalid URL format.");
            }
            int ownerId;
            List<Restaurant> restaurants;
            try {
                ownerId = Integer.parseInt(parts[4]);
                restaurants = restaurantService.getRestaurantByOwnerId(ownerId);
                String json = objectMapper.writeValueAsString(restaurants);
                sendResponse(exchange, 200, json);

            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid owner ID format");
            }

        }catch (Exception e){
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }

    }
}
