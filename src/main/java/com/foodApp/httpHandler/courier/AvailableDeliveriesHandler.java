package com.foodApp.httpHandler.courier;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Order;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.DeliveryService;
import com.foodApp.service.DeliveryServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class AvailableDeliveriesHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeliveryService deliveryService = new DeliveryServiceImpl();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        String token = extractToken(exchange);
        if(token == null) {
            sendResponse(exchange,401,Message.UNAUTHORIZED.get());
            return;
        }
        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
            if(!Role.COURIER.name().equals(jwt.getClaim("role").asString())) {
                sendResponse(exchange,403,Message.FORBIDDEN.get());
            }
        }catch(Exception e) {
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        try{
            List<Order> orders = deliveryService.getAvailableDeliveries();
            sendResponse(exchange,200,objectMapper.writeValueAsString(orders));
        }catch(Exception e) {
            sendResponse(exchange,500,Message.SERVER_ERROR.get());
        }

    }
}
