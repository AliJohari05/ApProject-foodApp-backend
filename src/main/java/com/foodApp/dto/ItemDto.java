package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemDto {

    @JsonProperty("item_id")
    private int itemId;

    @JsonProperty("quantity")
    private int quantity;

    public ItemDto() {}

    public ItemDto(int itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }


    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
