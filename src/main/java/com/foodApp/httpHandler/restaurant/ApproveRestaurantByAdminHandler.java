package com.foodApp.httpHandler.restaurant;

import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class ApproveRestaurantByAdminHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if(!exchange.getRequestMethod().equalsIgnoreCase("PATCH")) {
                sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            }
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length != 5) {
                sendResponse(exchange, 400, "Invalid URL format.");
                return;
            }

            int restaurantId;
            try {
                restaurantId = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid restaurant ID format");
                return;
            }
            restaurantService.approveRestaurant(restaurantId);
            sendResponse(exchange, 200, "Restaurant approved successfully");
        }catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }

    }
}
