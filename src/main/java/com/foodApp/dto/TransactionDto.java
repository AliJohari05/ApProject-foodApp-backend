package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.TransactionModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {
    private int id;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("user_id")
    private Integer userId;
    private BigDecimal amount;
    private String method;
    private String status;

    public TransactionDto(TransactionModel trx) {
        this.id = trx.getId();
        this.amount = trx.getAmount();
        this.method = trx.getMethod().name();
        this.status = trx.getStatus().name();
        this.orderId = trx.getOrderId(); // FIX: Add this line to copy orderId
        this.userId = trx.getUserId();   // FIX: Add this line to copy userId
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}