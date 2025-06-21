package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.OrderDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Order;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.OrderService;
import com.foodApp.service.OrderServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonMappingException;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class OrderHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderService orderService = new OrderServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        switch (path) {
            case "/orders":
                postHandle(exchange);
                break;
            case "/orders/history":
                getHandleHistory(exchange);
                break;
            default:
                // Handle /orders/{id}
                if (path.matches("/orders/\\d+")) {
                    getHandleId(exchange);
                } else {
                    sendResponse(exchange, 404, Message.ERROR_404.get());
                }
                break;
        }
    }

    private void postHandle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }

        String token = extractToken(exchange);
        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.CUSTOMER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return;
        }

        int customerId = Integer.parseInt(jwt.getSubject()); // Extract customer ID from JWT

        try (InputStream is = exchange.getRequestBody()) {
            OrderDto dto = objectMapper.readValue(is, OrderDto.class);

            if (!dto.isValid()) {
                sendResponse(exchange, 400, Message.MISSING_FIELDS.get());
                return;
            }

            Order submittedOrder = orderService.submitOrder(dto, customerId); // Call the new service method
            sendResponse(exchange, 200, objectMapper.writeValueAsString(submittedOrder)); // Send the created order as response

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (IllegalArgumentException e) { // Catch exceptions like insufficient stock
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }

    private void getHandleId(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }

        String token = extractToken(exchange);
        if (!isTokenValid(token, exchange)) return;

        // Extract ID from path
        String idString = exchange.getRequestURI().getPath().split("/")[2];
        int id = Integer.parseInt(idString);
        Order order = orderService.findById(id);
        if (order == null) {
            sendResponse(exchange, 404, Message.ERROR_404.get());
            return;
        }

        String jsonResponse = objectMapper.writeValueAsString(order);
        sendResponse(exchange, 200, jsonResponse);
    }

    private void getHandleHistory(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }

        String token = extractToken(exchange);
        if (!isTokenValid(token, exchange)) return;

        // Get order history
        List<Order> orders = orderService.findAll();
        String jsonResponse = objectMapper.writeValueAsString(orders);
        sendResponse(exchange, 200, jsonResponse);
    }

    private boolean isTokenValid(String token, HttpExchange exchange) throws IOException {
        if (token == null) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return false;
        }
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.CUSTOMER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return false;
            }
        } catch (Exception e) {
            sendResponse(exchange, 403, Message.FORBIDDEN.get());
            return false;
        }
        return true;
    }

    private boolean isOrderDtoValid(OrderDto dto) {
        return dto.getDeliveryAddress() != null && dto.getItems() != null && dto.getVendorId() != null && dto.isValid();
    }
}