package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryStatusUpdateRequestDto {
    @JsonProperty("status")
    String deliveryStatus;

    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    public boolean isValid() {
        if(deliveryStatus == null || deliveryStatus.isBlank()) {
            return false;
        }
        return deliveryStatus.equalsIgnoreCase("accepted") || deliveryStatus.equalsIgnoreCase("received")
                || deliveryStatus.equalsIgnoreCase("delivered");
    }
}
