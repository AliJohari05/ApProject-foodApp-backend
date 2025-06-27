package com.foodApp.httpHandler.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.foodApp.service.OrderService;
import com.foodApp.service.OrderServiceImpl;
import com.foodApp.util.QueryParser;
import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Collections;


public class AdminOrdersHandler extends BaseHandler implements HttpHandler {
    private final OrderService orderService = new OrderServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
                return;
            }
            String token = extractToken(exchange);

            if (token == null) {
                sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
                return;
            }
            DecodedJWT jwt;
            try {
                jwt = TokenService.verifyToken(token);
            } catch (Exception e) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
            if (!jwt.getClaim("role").asString().equals(Role.ADMIN.name())) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
                return;
            }

            Map<String, String> queryParams = QueryParser.parse(exchange.getRequestURI().getRawQuery());
            String search = queryParams.get("search");
            String vendor = queryParams.get("vendor");
            String courier = queryParams.get("courier");
            String customer = queryParams.get("customer");
            String statusString = queryParams.get("status");

            OrderStatus status = null;
            if (statusString != null && !statusString.isBlank()) {
                try {
                    status = OrderStatus.fromString(statusString);
                } catch (IllegalArgumentException e) {
                    sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", "Invalid status value: " + statusString)));
                    return;
                }
            }

            List<Order> orders;
            orders = orderService.findAllOrdersWithFilters(search, vendor, courier, customer, status);

            String jsonResponse = objectMapper.writeValueAsString(orders);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }
}