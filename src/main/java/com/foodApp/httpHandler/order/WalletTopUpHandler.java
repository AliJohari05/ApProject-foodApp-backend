package com.foodApp.httpHandler.order;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.WalletTopUpDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.TransactionModel;
import com.foodApp.model.PaymentMethod;
import com.foodApp.model.PaymentStatus;
import com.foodApp.security.TokenService;
import com.foodApp.service.TransactionService;
import com.foodApp.service.TransactionServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.util.Date;

public class WalletTopUpHandler extends BaseHandler implements HttpHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionService transactionService = new TransactionServiceImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
                return;
            }

            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
                return;
            }

            DecodedJWT jwt;
            try{
                jwt=TokenService.verifyToken(token);
            }catch (Exception e){
                sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
                return;
            }
            int userId = Integer.parseInt(jwt.getSubject());

            InputStream is = exchange.getRequestBody();
            WalletTopUpDto dto = objectMapper.readValue(is, WalletTopUpDto.class);

            if (!dto.isValid()) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }

            TransactionModel transaction = new TransactionModel();
            transaction.setUserId(userId);
            transaction.setAmount(dto.getAmount());
            transaction.setMethod(PaymentMethod.valueOf(dto.getMethod().toUpperCase()));
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setCreatedAt(new Date());

            transactionService.save(transaction);
            sendResponse(exchange, 200, Message.WALLET_TOPPED_UP.get());

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
