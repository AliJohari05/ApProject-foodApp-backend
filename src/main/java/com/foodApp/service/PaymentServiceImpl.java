package com.foodApp.service;

import com.foodApp.exception.InsufficientBalanceException;
import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;
import com.foodApp.model.TransactionModel;
import com.foodApp.model.User;
import com.foodApp.repository.OrderRepository;
import com.foodApp.repository.TransactionRepository;
import com.foodApp.repository.UserRepository;
import com.foodApp.repository.OrderRepositoryImpl;
import com.foodApp.repository.TransactionRepositoryImpl;
import com.foodApp.repository.UserRepositoryImp;

public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo = new OrderRepositoryImpl();
    private final UserRepository userRepo = new UserRepositoryImp();
    private final TransactionRepository transactionRepo = new TransactionRepositoryImpl();

    @Override
    public boolean processPayment(int userId, int orderId, String method) {
        Order order = orderRepo.findById(orderId);
        if (order == null || order.getCustomer().getUserId() != userId) {
            return false;
        }

        if (!method.equalsIgnoreCase("wallet") && !method.equalsIgnoreCase("paywall") &&!method.equalsIgnoreCase("online")) {
            return false; // یا throw new IllegalArgumentException("Invalid method");
        }

        if (method.equalsIgnoreCase("wallet")) {
            User user = userRepo.findById(userId);
            if (user.getWalletBalance().compareTo(order.getTotalPrice()) < 0) {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                throw new InsufficientBalanceException();
            }
            user.setWalletBalance(user.getWalletBalance().subtract(order.getTotalPrice()));
            order.setStatus(OrderStatus.PAID);
            userRepo.save(user);
        }

        order.setStatus(OrderStatus.PAID);
        orderRepo.save(order);

        TransactionModel transaction = new TransactionModel(
                userId,
                orderId,
                method.toLowerCase(),
                "success",
                order.getTotalPrice()
        );
        transactionRepo.save(transaction);

        return true;
    }
}
