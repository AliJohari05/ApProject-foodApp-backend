package com.foodApp.dto;

public class WalletTopUpDto {
    private String method;
    private double amount;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isValid() {
        return method != null && (method.equals("online") || method.equals("card")) && amount > 0;
    }
}
