package com.foodApp.dto;

import com.foodApp.model.TransactionModel;

import java.time.LocalDateTime;

public class TransactionDto {
    private int id;
    private int amount;
    private String method;
    private String status;
    private LocalDateTime createdAt;

    public TransactionDto(TransactionModel trx) {
        this.id = trx.getId();
        this.amount = trx.getAmount();
        this.method = trx.getMethod().name();
        this.status = trx.getStatus().name();
        this.createdAt = trx.getCreatedAt();
    }

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
