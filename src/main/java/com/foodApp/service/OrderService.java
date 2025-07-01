package com.foodApp.service;

import com.foodApp.dto.OrderDto;
import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;

import java.util.List;

public interface OrderService{
    public void save (Order order);
    public List<Order> findAll();
    public Order findById(int id);
    public List<Order> findByStatus(OrderStatus status);
    public void deleteById(int id);
    Order submitOrder(OrderDto orderDto, int customerId);
    List<Order> findAllOrdersWithFilters(String search, String vendorName, String courierName, String customerName, OrderStatus status);
    List<Order> getRestaurantOrders(Integer restaurantId, String search, String customerName, String courierName, OrderStatus status);
    void updateOrderStatusByRestaurant(Integer restaurantId, Integer orderId, String newStatusString);
}