package com.foodApp.repository;

import com.foodApp.model.Order;

import java.util.List;

public interface OrderRepository {
    void save(Order order);
    Order findById(int id);
    List<Order> findByCustomerId(int customerId);
    List<Order> findByRestaurantId(int restaurantId);
    void deleteById(int id);
}
