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
import com.foodApp.util.QueryParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DeliveryHistoryHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeliveryService deliveryService = new DeliveryServiceImpl();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange,405, objectMapper.writeValueAsString(Message.METHOD_NOT_ALLOWED.get()));
            return;
        }
        String token = extractToken(exchange);
        if(token == null) {
            sendResponse(exchange,401,objectMapper.writeValueAsString(Message.UNAUTHORIZED.get()));
            return;
        }
        DecodedJWT jwt;
        int courierId;
        try{
            jwt = TokenService.verifyToken(token);
            if(!Role.COURIER.name().equals(jwt.getClaim("role").asString())) {
                sendResponse(exchange,403,objectMapper.writeValueAsString(Message.FORBIDDEN.get()));
                return;
            }
            courierId = Integer.parseInt(jwt.getSubject());
        }catch(Exception e){
            sendResponse(exchange,403,objectMapper.writeValueAsString(Message.FORBIDDEN.get()));
            return;
        }
        Map<String, String> queryParams = QueryParser.parse(exchange.getRequestURI().getRawQuery());
        String search = queryParams.get("search");
        String vendor = queryParams.get("vendor");
        String user = queryParams.get("user");
        try {
            List<Order> deliveryHistory = deliveryService.getDeliveryHistory(courierId, search, vendor, user);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(deliveryHistory));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Message.SERVER_ERROR.get()));
        }

    }
}
