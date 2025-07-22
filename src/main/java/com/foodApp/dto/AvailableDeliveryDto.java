package com.foodApp.dto;

import com.foodApp.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AvailableDeliveryDto {
    private int id;
    private String deliveryAddress;
    private int customerId;
    private int restaurantId;
    private List<Integer> itemIds;
    private BigDecimal payPrice;
    private String status;
    private LocalDateTime createdAt;

    public AvailableDeliveryDto(Order order) {
        this.id = order.getId();
        this.deliveryAddress = order.getDeliveryAddress();
        this.customerId = order.getCustomer().getUserId();
        this.restaurantId = order.getRestaurant().getId();
        this.itemIds = order.getOrderItems().stream()
                .map(oi -> oi.getMenuItem().getId())
                .collect(Collectors.toList());
        this.payPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.createdAt = order.getCreatedAt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<Integer> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Integer> itemIds) {
        this.itemIds = itemIds;
    }

    public BigDecimal getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
