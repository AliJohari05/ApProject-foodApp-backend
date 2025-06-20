package com.foodApp;

import com.foodApp.httpHandler.admin.AdminTransactionsHandler;
import com.foodApp.httpHandler.admin.AdminUserHandler;
import com.foodApp.httpHandler.buyer.ListOfVendorsHandler;
import com.foodApp.httpHandler.courier.AvailableDeliveriesHandler;
import com.foodApp.httpHandler.courier.DeliveryHistoryHandler;
import com.foodApp.httpHandler.courier.DeliveryStatusUpdateHandler;
import com.foodApp.httpHandler.order.PaymentHandler;
import com.foodApp.httpHandler.order.TransactionHistoryHandler;
import com.foodApp.httpHandler.restaurant.RestaurantHandler;
import com.foodApp.httpHandler.user.LoginHandler;
import com.foodApp.httpHandler.user.LogoutHandler;
import com.foodApp.httpHandler.user.ProfileHandler;
import com.foodApp.httpHandler.user.SignUpHandler;
import com.sun.net.httpserver.HttpServer;
import com.foodApp.httpHandler.restaurant.menuItemHandler;
import com.foodApp.httpHandler.order.WalletTopUpHandler;
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
            server.createContext("/restaurants", new RestaurantHandler());
            server.createContext("/restaurants/", new RestaurantHandler());
            server.createContext("/transactions", new TransactionHistoryHandler());
            server.createContext("/wallet/top-up", new WalletTopUpHandler());
            server.createContext("/payment/online", new PaymentHandler());
            server.createContext("/deliveries/available", new AvailableDeliveriesHandler());
            server.createContext("/deliveries", new DeliveryStatusUpdateHandler());
            server.createContext("/deliveries/history", new DeliveryHistoryHandler());
            server.createContext("/vendors", new ListOfVendorsHandler());
            //server.createContext("/vendors/", new DeliveryHistoryHandler());
            server.createContext("/admin/transactions", new AdminTransactionsHandler());



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

