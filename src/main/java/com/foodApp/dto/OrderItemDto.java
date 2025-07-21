package com.foodApp.dto;

import com.foodApp.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemDto {
    private String menuItemName;
    private int quantity;
    private BigDecimal priceAtOrder;

    public OrderItemDto(OrderItem item) {
        this.menuItemName = item.getMenuItem().getName();
        this.quantity = item.getQuantity();
        this.priceAtOrder = item.getPriceAtOrder();
    }

    public String getMenuItemName() { return menuItemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
}
