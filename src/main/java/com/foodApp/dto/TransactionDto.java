package com.foodApp.dto;

import com.foodApp.model.TransactionModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {
    private int id;
    private BigDecimal amount;
    private String method;
    private String status;

    public TransactionDto(TransactionModel trx) {
        this.id = trx.getId();
        this.amount = trx.getAmount();
        this.method = trx.getMethod().name();
        this.status = trx.getStatus().name();
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

}
