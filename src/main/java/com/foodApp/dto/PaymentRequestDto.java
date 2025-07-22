package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequestDto {
    @JsonProperty("order_id")
    private int orderId;
    private String method;

    public boolean isValid() {
        return orderId > 0 && (method != null && (method.equalsIgnoreCase("wallet") || method.equalsIgnoreCase("online")));
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public int getOrderId() {
        return orderId;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public String getMethod() {
        return method;
    }

}


