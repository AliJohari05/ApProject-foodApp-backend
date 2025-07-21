package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodApp.dto.OrderDetailsDto;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final ObjectMapper safeMapper;
    private final OrderService orderService = new OrderServiceImpl();

    public OrderHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // ✅ پشتیبانی از LocalDateTime
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // نمایش تاریخ خوانا

        safeMapper = new ObjectMapper();
        safeMapper.registerModule(new JavaTimeModule());
        safeMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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
                if (path.matches("/orders/\\d+")) {
                    getHandleId(exchange);
                } else {
                    sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                }
                break;
        }
    }

    private void postHandle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }

        String token = extractToken(exchange);
        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }

        int customerId = Integer.parseInt(jwt.getSubject());

        try (InputStream is = exchange.getRequestBody()) {
            OrderDto dto;
            try {
                dto = objectMapper.readValue(is, OrderDto.class);
            } catch (JsonMappingException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }

            if (!dto.isValid()) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get())));
                return;
            }

            Order submittedOrder = orderService.submitOrder(dto, customerId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(submittedOrder));

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get())));
        }
    }

    private void getHandleId(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            return;
        }

        String token = extractToken(exchange);
        if (!isTokenValid(token, exchange)) return;

        String idString = exchange.getRequestURI().getPath().split("/")[2];
        int id = Integer.parseInt(idString);
        Order order = orderService.findById(id);
        if (order == null) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
            return;
        }

        OrderDetailsDto dto = new OrderDetailsDto(order);
        String jsonResponse = objectMapper.writeValueAsString(dto);
        sendResponse(exchange, 200, jsonResponse);
    }


    private void getHandleHistory(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            return;
        }

        String token = extractToken(exchange);
        if (!isTokenValid(token, exchange)) return;

        List<Order> orders = orderService.findAll();
        String jsonResponse = objectMapper.writeValueAsString(orders);
        sendResponse(exchange, 200, jsonResponse);
    }

    private boolean isTokenValid(String token, HttpExchange exchange) throws IOException {
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return false;
        }
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return false;
            }
        } catch (Exception e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
            return false;
        }
        return true;
    }
}
