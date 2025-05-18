package com.foodApp.httpHandler.restaurant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.model.Restaurant;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
public class RegisterRestaurantHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{
            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
                sendResponse(exchange,405,"Not Allowed");
            }
            InputStream body = exchange.getRequestBody();
            Restaurant restaurant = objectMapper.readValue(body, Restaurant.class);
            if(restaurant.getOwner() == null || restaurant.getName() == null || restaurant.getAddress() == null){
                sendResponse(exchange, 400, "Missing required fields (name or owner or address)");
            }
            restaurantService.registerRestaurant(restaurant);
            sendResponse(exchange, 200, "Restaurant registered successfully (pending approval)");


        }catch (Exception e){
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }


    private void sendResponse(HttpExchange exchange,int statusCode ,String message) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte [] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode,bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
