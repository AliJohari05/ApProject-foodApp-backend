package com.foodApp.dto;

public class DeliveryStatusUpdateRequestDto {
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
