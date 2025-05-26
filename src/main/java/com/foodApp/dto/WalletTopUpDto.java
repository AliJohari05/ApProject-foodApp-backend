package com.foodApp.dto;

import java.math.BigDecimal;

public class WalletTopUpDto {
    private BigDecimal amount;
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isValid() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

}
