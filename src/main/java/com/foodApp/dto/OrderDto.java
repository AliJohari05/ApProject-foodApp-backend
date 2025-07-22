package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("customer_id")
    private Integer customerId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty("items")
    private List<ItemDto> items;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("courier_id")
    private Integer courierId;

    public OrderDto() {
    }

    public OrderDto(Integer id, Integer customerId, OrderStatus status, BigDecimal totalPrice) {
        this.id = id;
        this.customerId = customerId;
        this.status = String.valueOf(status);
        this.totalPrice = totalPrice;
    }
    public OrderDto(int id, int customerId, OrderStatus status, BigDecimal payPrice, String deliveryAddress, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = String.valueOf(status);
        this.totalPrice = payPrice;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = createdAt;
    }
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public boolean isValid() {
        return deliveryAddress != null && !deliveryAddress.isBlank() && vendorId != null && items != null && !items.isEmpty();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Integer getCourierId() {
        return courierId;
    }
    public void setCourierId(Integer courierId) {
        this.courierId = courierId;
    }
}