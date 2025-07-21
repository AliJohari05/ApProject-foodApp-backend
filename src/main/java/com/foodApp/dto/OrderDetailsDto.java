package com.foodApp.dto;


import com.foodApp.model.Order;
import com.foodApp.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDetailsDto {
    private int id;
    private String status;
    private BigDecimal totalPrice;
    private BigDecimal taxFee;
    private BigDecimal courierFee;
    private BigDecimal additionalFee;
    private BigDecimal rawPrice;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
    private String restaurantName;
    private String customerUsername;
    private List<OrderItemDto> items;

    public OrderDetailsDto(Order order) {
        this.id = order.getId();
        this.status = order.getStatus().name();
        this.totalPrice = order.getTotalPrice();
        this.taxFee = order.getTaxFee();
        this.courierFee = order.getCourierFee();
        this.additionalFee = order.getAdditionalFee();
        this.rawPrice = order.getRawPrice();
        this.deliveryAddress = order.getDeliveryAddress();
        this.notes = order.getNotes();
        this.createdAt = order.getCreatedAt();
        this.restaurantName = order.getRestaurant().getName();
        this.customerUsername = order.getCustomer().getName();
        this.items = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }

    // Getters (در صورت نیاز می‌تونی setters هم اضافه کنی)
    public int getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BigDecimal getTaxFee() { return taxFee; }
    public BigDecimal getCourierFee() { return courierFee; }
    public BigDecimal getAdditionalFee() { return additionalFee; }
    public BigDecimal getRawPrice() { return rawPrice; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRestaurantName() { return restaurantName; }
    public String getCustomerUsername() { return customerUsername; }
    public List<OrderItemDto> getItems() { return items; }
}

