package com.foodApp;

import com.foodApp.httpHandler.restaurant.ApproveRestaurantByAdminHandler;
import com.foodApp.httpHandler.restaurant.ApprovedRestaurantHandler;
import com.foodApp.httpHandler.restaurant.GetRestaurantsByOwnerHandler;
import com.foodApp.httpHandler.user.loginHandler;
import com.foodApp.httpHandler.user.signUpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/auth/register", new signUpHandler());
            server.createContext("/auth/login", new loginHandler());
            server.createContext("/Food4U/restaurant", new loginHandler());
            server.createContext("/Food4U/restaurant/approved", new ApprovedRestaurantHandler());
            server.createContext("/Food4U/restaurant/owner", new GetRestaurantsByOwnerHandler());
            server.createContext("/Food4U/admin/restaurant/approved", new ApproveRestaurantByAdminHandler());


            server.setExecutor(null);
            server.start();

            System.out.println("Server started on http://localhost:8080");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

