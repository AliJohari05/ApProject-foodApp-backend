package com.foodApp.service;

import com.foodApp.model.Delivery;
import com.foodApp.model.DeliveryStatus;
import com.foodApp.model.Order;

import java.util.List;

public interface DeliveryService {
    List<Order> getAvailableDeliveries();
    Delivery updateDeliveryStatus(int orderId, int courierId, DeliveryStatus newInternalStatus);
    List<Order> getDeliveryHistory(int courierId, String search, String vendorName, String customerName);
}
