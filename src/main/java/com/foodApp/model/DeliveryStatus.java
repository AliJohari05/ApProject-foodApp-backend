package com.foodApp.model;

public enum DeliveryStatus {
    // Statuses set by the system or restaurant
    ORDER_PREPARING,        // Order being prepared by the restaurant
    READY_FOR_PICKUP,     // Order ready and waiting for courier

    // Statuses set by the courier via API
    COURIER_ACCEPTED,     // The courier has accepted the order
    PICKED_UP,            // The courier has picked up the order from the restaurant.
    IN_TRANSIT,           // The courier is on its way to the customer.
    DELIVERED,            // The courier has delivered the order to the customer

    CANCELED_BY_COURIER,
    CANCELED_BY_SYSTEM;


    public static DeliveryStatus fromApiAction(String apiStatus) {
        if ("accepted".equalsIgnoreCase(apiStatus)) {
            return COURIER_ACCEPTED;
        } else if ("received".equalsIgnoreCase(apiStatus)) {
            return PICKED_UP;
        } else if ("delivered".equalsIgnoreCase(apiStatus)) {
            return DELIVERED;
        }
        throw new IllegalArgumentException("Invalid API status for delivery: " + apiStatus);
    }
}