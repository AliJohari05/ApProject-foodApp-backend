package com.foodApp.httpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
public class BaseHandler {
    protected void sendJson(HttpExchange exchange, int statusCode, String message) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, messageBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(messageBytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
