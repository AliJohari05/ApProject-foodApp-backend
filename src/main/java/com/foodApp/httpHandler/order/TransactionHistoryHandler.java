package com.foodApp.httpHandler.order;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.TransactionDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.TransactionModel;
import com.foodApp.security.TokenService;
import com.foodApp.service.TransactionService;
import com.foodApp.service.TransactionServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryHandler extends BaseHandler implements HttpHandler {
    private final TransactionService transactionService = new TransactionServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        }catch(Exception e) {
            sendResponse(exchange,401,Message.UNAUTHORIZED.get());
            return;
        }
        int userId = Integer.parseInt(jwt.getSubject());
        try {
            List<TransactionModel> transactionsList = transactionService.getByUserId(userId);
            List<TransactionDto> dtoList = new ArrayList<TransactionDto>();
            for(TransactionModel transactionModel : transactionsList) {
                dtoList.add(new TransactionDto(transactionModel));
            }
            String json = objectMapper.writeValueAsString(dtoList);
            sendResponse(exchange,200, json);

        }catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange,500,Message.SERVER_ERROR.get());
        }



    }
}
