package com.foodApp;

import com.foodApp.httpHandler.admin.AdminCouponsHandler;
import com.foodApp.httpHandler.admin.AdminOrdersHandler;
import com.foodApp.httpHandler.admin.AdminTransactionsHandler;
import com.foodApp.httpHandler.admin.AdminUserHandler;
import com.foodApp.httpHandler.buyer.*;
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
import com.foodApp.httpHandler.order.WalletTopUpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
            server.createContext("/admin/transactions", new AdminTransactionsHandler());
            server.createContext("/admin/orders", new AdminOrdersHandler());
            server.createContext("/orders", new OrderHandler());
            server.createContext("/orders/", new OrderHandler());
            server.createContext("/orders/history", new OrderHandler());
            server.createContext("/items", new ItemHandler());
            server.createContext("/items/", new ItemHandler());
            server.createContext("/coupons", new CouponHandler());
            server.createContext("/favorites", new FavoriteHandler());
            server.createContext("/favorites/", new FavoriteHandler());
            server.createContext("/ratings", new RatingHandler());
            server.createContext("/ratings/", new RatingHandler());
            server.createContext("/admin/coupons", new AdminCouponsHandler());
            server.createContext("/admin/coupons/", new AdminCouponsHandler());



// Static file serving for profile images
            server.createContext("/uploads", exchange -> {
                String requestedPath = exchange.getRequestURI().getPath().replaceFirst("/uploads", "");
                File file = new File("uploads", requestedPath);

                if (file.exists() && file.isFile()) {
                    String contentType = guessContentType(file.getName());
                    exchange.getResponseHeaders().add("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, file.length());
                    try (OutputStream os = exchange.getResponseBody();
                         FileInputStream fis = new FileInputStream(file)) {
                        fis.transferTo(os);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            });


            server.setExecutor(null);
            server.start();

            System.out.println("Server started on http://localhost:8080");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

}

