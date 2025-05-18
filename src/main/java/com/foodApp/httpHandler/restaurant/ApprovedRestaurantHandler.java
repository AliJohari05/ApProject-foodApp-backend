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
import java.util.List;

public class ApprovedRestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if(!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            }
            List<Restaurant> allRestaurantApproved = restaurantService.getAllApprovedRestaurants();
            String responseJson = objectMapper.writeValueAsString(allRestaurantApproved);
            sendResponse(exchange,200, responseJson);

        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

}
