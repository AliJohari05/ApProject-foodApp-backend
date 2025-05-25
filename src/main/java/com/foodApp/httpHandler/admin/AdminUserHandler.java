package com.foodApp.httpHandler.admin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.StatusDto;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AdminUserHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if(path.equals("/admin/users")) {
            getHandel(exchange);
        }
        else if (path.matches("/admin/users/\\d+/status")) {
            patchHandel(exchange);
        }

        else{
            sendResponse(exchange,404, Message.ERROR_404.get());
        }

    }
    private void getHandel(HttpExchange exchange) throws IOException {
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
        try{
            jwt = TokenService.verifyToken(token);
        }catch (Exception e) {
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        if (!jwt.getClaim("role").asString().equals(Role.ADMIN.name())) {
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
    private void patchHandel(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equals("PATCH")) {
            sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        String token = extractToken(exchange);
        if(token == null) {
            sendResponse(exchange,401,Message.UNAUTHORIZED.get());
            return;
        }
        DecodedJWT jwt;
        try{
            jwt = TokenService.verifyToken(token);
        }catch (Exception e) {
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        if (!jwt.getClaim("role").asString().equals(Role.ADMIN.name())) {
            sendResponse(exchange,403,Message.FORBIDDEN.get());
            return;
        }
        String[] segments = exchange.getRequestURI().getPath().split("/");
        String idStr = segments[3];
        try {
            int userId = Integer.parseInt(idStr);
            InputStream is = exchange.getRequestBody();
            StatusDto dto = objectMapper.readValue(is, StatusDto.class);

            if (!dto.isValid()) {
                sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                return;
            }

            boolean updated = userService.updateUserStatus(userId, dto.getStatus());
            if (!updated) {
                sendResponse(exchange, 404, Message.USER_NOT_FOUND.get());
                return;
            }

            sendResponse(exchange, 200, Message.STATUS_UPDATED.get());
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
