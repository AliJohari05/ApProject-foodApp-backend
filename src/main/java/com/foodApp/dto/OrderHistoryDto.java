package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.Order;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class OrderHistoryDto {
    private int id;
    private String status;
    @JsonProperty("vendor_name")
    private String vendorName;
    @JsonProperty("pay_price")
    private BigDecimal payPrice;
    @JsonProperty("created_at")
    private String createdAt;

    public OrderHistoryDto(Order order) {
        this.id = order.getId();
        this.status = order.getStatus().name();
        this.vendorName = order.getRestaurant() != null ? order.getRestaurant().getName() : "N/A";
        this.payPrice = order.getTotalPrice();

        if (order.getCreatedAt() != null) {
            this.createdAt = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } else {
            this.createdAt = "N/A";
        }
    }

    public int getId() { return id; }
    public String getStatus() { return status; }
    public String getVendorName() { return vendorName; }
    public BigDecimal getPayPrice() { return payPrice; }
    public String getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public void setPayPrice(BigDecimal payPrice) { this.payPrice = payPrice; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
