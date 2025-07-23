package com.foodApp.service;

import com.foodApp.exception.DeliveryNotFoundException;
import com.foodApp.exception.InvalidDeliveryStatusTransitionException;
import com.foodApp.exception.OrderNotFoundException;
import com.foodApp.exception.UnauthorizedAccessException;
import com.foodApp.model.*;
import com.foodApp.repository.*;

import java.time.LocalDateTime;
import java.util.List;

public class DeliveryServiceImpl implements DeliveryService{
    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final DeliveryRepository deliveryRepository = new DeliveryRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImp();

    @Override
    public List<Order> getAvailableDeliveries() {
        List<OrderStatus> statuses = List.of(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.COURIER_ASSIGNED,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED_TO_CUSTOMER
        );

        return orderRepository.findOrdersByStatuses(statuses);
    }


    @Override
    public Delivery updateDeliveryStatus(int orderId, int courierId, DeliveryStatus newInternalStatus) {
        User courier = userRepository.findById(courierId);
        if(courier == null || courier.getRole() != Role.COURIER) {
            throw new UnauthorizedAccessException("User is not authorized or not a courier.");
        }

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        Delivery delivery = deliveryRepository.findByOrderId(orderId);
        if(newInternalStatus == DeliveryStatus.COURIER_ACCEPTED) {
            if(delivery != null && delivery.getDeliveryPerson().getUserId() != courier.getUserId()&&(
                    delivery.getStatus() == DeliveryStatus.COURIER_ACCEPTED ||
                            delivery.getStatus() == DeliveryStatus.PICKED_UP ||
                            delivery.getStatus() == DeliveryStatus.IN_TRANSIT
            )) {
                throw new InvalidDeliveryStatusTransitionException("Delivery already assigned to another courier.");
            }
        }

        if (delivery == null) {

            if (newInternalStatus == DeliveryStatus.COURIER_ACCEPTED) {
                if (!OrderStatus.READY_FOR_PICKUP.equals(order.getStatus())) {
                    throw new InvalidDeliveryStatusTransitionException("Order is not in READY_FOR_PICKUP state.");
                }
                delivery = new Delivery();
                delivery.setOrder(order);
                delivery.setDeliveryPerson(courier);
                delivery.setStatus(DeliveryStatus.COURIER_ACCEPTED);
                delivery.setAssignedAt(LocalDateTime.now());
                delivery.setUpdatedAt(LocalDateTime.now());
                deliveryRepository.save(delivery);
                //change status order
                order.setStatus(OrderStatus.COURIER_ASSIGNED);
                orderRepository.save(order);
                return delivery;
            } else {
                throw new DeliveryNotFoundException("Delivery record not found for order: " + orderId);
            }
        }

        if (delivery.getDeliveryPerson().getUserId() != courierId) {
            throw new UnauthorizedAccessException("Courier not assigned to this delivery.");
        }

        if (!isValidTransition(delivery.getStatus(), newInternalStatus)) {
            throw new InvalidDeliveryStatusTransitionException("Cannot transition from " + delivery.getStatus() + " to " + newInternalStatus);
        }

        delivery.setStatus(newInternalStatus);
        delivery.setUpdatedAt(LocalDateTime.now());

        if (newInternalStatus == DeliveryStatus.PICKED_UP) {
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            orderRepository.save(order);
        } else if (newInternalStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            order.setStatus(OrderStatus.DELIVERED_TO_CUSTOMER);
            orderRepository.save(order);
        }

        deliveryRepository.save(delivery);
        return delivery;
    }

    private boolean isValidTransition(DeliveryStatus current, DeliveryStatus next) {
        if (current == DeliveryStatus.READY_FOR_PICKUP && next == DeliveryStatus.COURIER_ACCEPTED) return true;
        if (current == DeliveryStatus.COURIER_ACCEPTED && next == DeliveryStatus.PICKED_UP) return true;
        if (current == DeliveryStatus.PICKED_UP && (next == DeliveryStatus.IN_TRANSIT || next == DeliveryStatus.DELIVERED)) return true;
        if (current == DeliveryStatus.IN_TRANSIT && next == DeliveryStatus.DELIVERED) return true;
        return false;

    }

    @Override
    public List<Order> getDeliveryHistory(int courierId, String search, String vendorName, String customerName) {
        User courier = userRepository.findById(courierId);
        if (courier == null || courier.getRole() != Role.COURIER) {
            throw new UnauthorizedAccessException("Courier not found or not authorized.");
        }
        String courierName = courier.getName(); // Get courier's name for filtering

        return orderRepository.findOrdersWithFilters(search, vendorName, courierName, customerName, null);
    }
}