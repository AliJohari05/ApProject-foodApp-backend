package com.foodApp.httpHandler.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.query.sqm.mutation.internal.Handler;

import java.io.IOException;

public class AdminOrdersHandler extends BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
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
            if (!jwt.getClaim("role").asString().equals(Role.ADMIN.name())) {
                sendResponse(exchange, 403, Message.UNAUTHORIZED.get());
                return;
            }








        } catch (Exception e) {
            sendResponse(exchange,500,Message.SERVER_ERROR.get());
        }

    }
}
