package com.foodApp.repository;

import com.foodApp.model.OrderItem;
import java.util.List;

public interface OrderItemRepository {
    void save(OrderItem item);
    List<OrderItem> findByOrderId(int orderId);
    void deleteByOrderId(int orderId);
}
