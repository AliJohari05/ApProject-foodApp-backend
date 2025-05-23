package com.foodApp.httpHandler.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.UserProfileDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.security.TokenService;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminUserListHandler extends BaseHandler implements HttpHandler {
    private final UserService userService = new UserServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
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
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        if(!jwt.getClaim("role").asString().equalsIgnoreCase(Role.ADMIN.name())){
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        try {
            List<User> users = userService.findAllUsers();
            List<UserProfileDto> dtoList = new ArrayList<>();
            for(User user : users) {
                dtoList.add(new UserProfileDto(user));
            }
            String json = objectMapper.writeValueAsString(dtoList);
            sendResponse(exchange,200,json);

        }catch (Exception e) {
            sendResponse(exchange,500,Message.SERVER_ERROR.get());

        }
    }
}
