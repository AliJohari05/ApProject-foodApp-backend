package com.foodApp.service;

public interface PaymentService {
    boolean processPayment(int userId, int orderId, String method);
}
