package com.foodApp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;//Representing a request and response
import com.sun.net.httpserver.HttpHandler;//Interface for creating route handlers

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;//Specifying the server address and port

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/hello", new HelloHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("🚀 Server started on http://localhost:8080");
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "hiiii";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
