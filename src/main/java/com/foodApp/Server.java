package com.foodApp;

import com.foodApp.httpHandler.admin.AdminUserHandler;
import com.foodApp.httpHandler.order.TransactionHistoryHandler;
import com.foodApp.httpHandler.restaurant.ApproveRestaurantByAdminHandler;
import com.foodApp.httpHandler.restaurant.ApprovedRestaurantHandler;
import com.foodApp.httpHandler.restaurant.GetRestaurantsByOwnerHandler;
import com.foodApp.httpHandler.restaurant.RegisterRestaurantHandler;
import com.foodApp.httpHandler.user.LoginHandler;
import com.foodApp.httpHandler.user.LogoutHandler;
import com.foodApp.httpHandler.user.ProfileHandler;
import com.foodApp.httpHandler.user.SignUpHandler;
import com.sun.net.httpserver.HttpServer;
import com.foodApp.httpHandler.restaurant.menuItemHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/auth/register", new SignUpHandler());
            server.createContext("/auth/login", new LoginHandler());
            server.createContext("/auth/profile", new ProfileHandler());
            server.createContext("/auth/logout", new LogoutHandler());
            server.createContext("/admin/users", new AdminUserHandler());
            server.createContext("/admin/users/", new AdminUserHandler());
            server.createContext("/restaurants", new RegisterRestaurantHandler());
            server.createContext("/restaurants/mine", new RegisterRestaurantHandler());
            server.createContext("/transactions", new TransactionHistoryHandler());
            //server.createContext("/wallet/top-up", new ApprovedRestaurantHandler());
            //server.createContext("/payment/online", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/approved", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/approved", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/approved", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/approved", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/approved", new ApprovedRestaurantHandler());
            //server.createContext("/restaurant/owner", new GetRestaurantsByOwnerHandler());
            //server.createContext("/admin/restaurant/approved", new ApproveRestaurantByAdminHandler());


            server.setExecutor(null);
            server.start();

            System.out.println("Server started on http://localhost:8080");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

