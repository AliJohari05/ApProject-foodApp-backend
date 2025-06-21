package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderDto {

    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty("items")
    private List<ItemDto> items;

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public boolean isValid() {
        return deliveryAddress != null && !deliveryAddress.isBlank() && vendorId != null && items != null && !items.isEmpty();
    }
}