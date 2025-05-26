package com.foodApp.httpHandler.order;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.PaymentRequestDto;
import com.foodApp.exception.InsufficientBalanceException;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.security.TokenService;
import com.foodApp.service.PaymentService;
import com.foodApp.service.PaymentServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class PaymentHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentService paymentService = new PaymentServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }

        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
        } catch (Exception e) {
            sendResponse(exchange, 403, Message.FORBIDDEN.get());
            return;
        }

        int userId = Integer.parseInt(jwt.getSubject());

        PaymentRequestDto dto = objectMapper.readValue(exchange.getRequestBody(), PaymentRequestDto.class);
        if (!dto.isValid()) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
            return;
        }

        try {
            boolean success = paymentService.processPayment(userId, dto.getOrderId(), dto.getMethod());
            if (success) {
                sendResponse(exchange, 200, Message.PAYMENT_SUCCESS.get());
            } else {
                sendResponse(exchange, 404, Message.UNAUTHORIZED.get());
            }
        } catch (InsufficientBalanceException e) {
            sendResponse(exchange, 400, ("Insufficient balance"));
        } catch (Exception e) {
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
