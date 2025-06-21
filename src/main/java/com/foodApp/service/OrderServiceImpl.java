package com.foodApp.service;

import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;
import com.foodApp.repository.OrderRepository;
import com.foodApp.repository.OrderRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository = new OrderRepositoryImpl();

    @Override
    public void save(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }


    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }


    @Override
    public Order findById(int id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findOrdersByStatus(status);
    }

    @Override
    public void deleteById(int id) {
        orderRepository.deleteById(id);
    }

}
