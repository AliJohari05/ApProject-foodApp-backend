package com.foodApp.repository;

import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;

import java.util.List;

public interface OrderRepository {
    void save(Order order);
    Order findById(int id);
    List<Order> findOrdersByStatus(OrderStatus status);
    List<Order> findAllById(List<Integer> orderIds);
    List<Order> findByCustomerId(int customerId);
    List<Order> findByRestaurantId(int restaurantId);
    void deleteById(int id);
    List<Order> findOrdersByIdsAndFilters(List<Integer> orderIds, String searchTerm, String vendorName, String customerName);
}
