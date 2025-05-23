package com.foodApp.httpHandler.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class LogoutHandler extends BaseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if(contentType == null || !contentType.equals("application/json")) {
            sendResponse(exchange,415,Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        String Token = extractToken(exchange);
        if(Token == null) {
            sendResponse(exchange,401,Message.UNAUTHORIZED.get());
            return;
        }
        try{
            DecodedJWT jwt = TokenService.verifyToken(Token);
            sendResponse(exchange,200,Message.LOGOUT_SUCCESS.get());
        }catch (JWTVerificationException e) {
            sendResponse(exchange, 403, (Message.FORBIDDEN.get()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, (Message.SERVER_ERROR.get()));
        }

    }
}
