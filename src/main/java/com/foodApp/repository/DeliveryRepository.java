package com.foodApp.repository;
import com.foodApp.model.DeliveryStatus;

import com.foodApp.model.Delivery;
import com.foodApp.model.Order;

import java.util.List;

public interface DeliveryRepository {
    void save(Delivery delivery);
    Delivery findById(int id);
    List<Delivery> findByDeliveryPersonId(int deliveryPersonId);
    Delivery findByOrderId(int orderId);
    void updateStatus(int deliveryId, DeliveryStatus status);

    List<Delivery> findByCourierId(int courierId);

}
