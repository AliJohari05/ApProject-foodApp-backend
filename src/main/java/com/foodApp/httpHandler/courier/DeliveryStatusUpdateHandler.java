package com.foodApp.httpHandler.courier;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.DeliveryStatusUpdateRequestDto;
import com.foodApp.exception.DeliveryNotFoundException;
import com.foodApp.exception.InvalidDeliveryStatusTransitionException;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Delivery;
import com.foodApp.model.DeliveryStatus;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.DeliveryService;
import com.foodApp.service.DeliveryServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DeliveryStatusUpdateHandler extends BaseHandler  implements HttpHandler {
    private final DeliveryService deliveryService = new DeliveryServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if(!path.matches("/delivery/\\d+")){
            sendResponse(exchange,404,Message.ERROR_404.get());
        }
        if(!exchange.getRequestMethod().equalsIgnoreCase("PATCH")) {
            sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        String token = extractToken(exchange);
        if(token == null) {
            sendResponse(exchange,401,Message.UNAUTHORIZED.get());
            return;
        }
        DecodedJWT jwt;
        int courierId;
        try {
            jwt = TokenService.verifyToken(token);
            if(!Role.COURIER.name().equals(jwt.getClaim("role").asString())) {
                sendResponse(exchange,403,Message.FORBIDDEN.get());
            }
            courierId = Integer.parseInt(jwt.getSubject());

        }catch(Exception e) {
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        String[] parts = path.split("/");
        int orderId;
        try{
            orderId = Integer.parseInt(parts[parts.length-1]);
        }catch(Exception e) {
            sendResponse(exchange,400,Message.FORBIDDEN.get());
            return;
        }
        InputStream is = exchange.getRequestBody();
        DeliveryStatusUpdateRequestDto dto = objectMapper.readValue(is, DeliveryStatusUpdateRequestDto.class);
        if(!dto.isValid()){
            sendResponse(exchange,400,Message.INVALID_INPUT.get());
            return;
        }
        try {
            DeliveryStatus newInternalStatus = DeliveryStatus.fromApiAction(dto.getDeliveryStatus());
            Delivery updatedDelivery = deliveryService.updateDeliveryStatus(orderId, courierId, newInternalStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("message", Message.CHANGED_STATUS_SUCCESS);
            response.put("order", updatedDelivery.getOrder());
            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
        }catch (OrderNotFoundException | DeliveryNotFoundException e) {
            sendResponse(exchange, 404, Message.ERROR_404.get());
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, Message.FORBIDDEN.get());
        } catch (InvalidDeliveryStatusTransitionException e) {
            sendResponse(exchange, 409,Message.Delivery_ALREDY_ASSIGNED.get());
        } catch (Exception e) {
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }

    }
}
