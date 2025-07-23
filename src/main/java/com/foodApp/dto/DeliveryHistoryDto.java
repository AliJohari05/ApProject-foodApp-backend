package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class DeliveryHistoryDto {
    @JsonProperty("order_id")
    private Integer orderId;

    @JsonProperty("vendor_id")
    private Integer vendorId;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("status")
    private String status;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public DeliveryHistoryDto() {}

    public DeliveryHistoryDto(Integer orderId, Integer vendorId, String deliveryAddress, String status, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.vendorId = vendorId;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
