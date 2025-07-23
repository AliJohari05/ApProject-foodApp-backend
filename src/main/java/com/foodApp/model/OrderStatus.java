package com.foodApp.model;

public enum OrderStatus {
    // Initial and vendor-related statuses
    SUBMITTED,              // Order placed by user
    PENDING_PAYMENT,        // Awaiting payment (if separate from submission)
    PAYMENT_FAILED,         // Payment attempt was unsuccessful
    PAID,                   // Payment was successful
    WAITING_VENDOR_ACCEPTANCE, // Waiting for restaurant to confirm
    ACCEPTED_BY_VENDOR,     // Restaurant accepted the order
    REJECTED_BY_VENDOR,     // Restaurant rejected the order
    PREPARING,              // Restaurant is preparing the order
    READY_FOR_PICKUP,       // Order is ready for courier pickup

    // Delivery-related statuses
    FINDING_COURIER,        // System is looking for a courier
    COURIER_ASSIGNED,       // Courier has been assigned to the order
    OUT_FOR_DELIVERY,       // Courier has picked up and is en route
    DELIVERED_TO_CUSTOMER,  // Order delivered to the customer

    // Final/Cancellation statuses
    CANCELLED_BY_USER,      // User cancelled the order (from YAML: "cancelled")
    CANCELLED_BY_VENDOR,    // Restaurant cancelled the order
    UNPAID_AND_CANCELLED;   // Order was not paid and subsequently cancelled (from YAML)

    /**
     * Converts a string representation of a status to the OrderStatus enum.
     * This method first tries a direct name match and then falls back to YAML specific values.
     * @param text The string representation of the status.
     * @return The corresponding OrderStatus enum.
     * @throws IllegalArgumentException if the text does not correspond to any known status.
     */
    public static OrderStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Status text cannot be null or empty.");
        }

        for (OrderStatus b : OrderStatus.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }

        // Handle specific string values from YAML or other external sources
        switch (text.toLowerCase().trim()) {
            case "submitted": return SUBMITTED;
            case "unpaid and cancelled": return UNPAID_AND_CANCELLED;
            case "waiting vendor": return WAITING_VENDOR_ACCEPTANCE;
            case "cancelled": return CANCELLED_BY_USER; // Defaulting "cancelled" from YAML to user cancellation
            case "finding courier": return FINDING_COURIER;
            case "on the way": return OUT_FOR_DELIVERY;
            case "completed": return DELIVERED_TO_CUSTOMER;
            default:
                throw new IllegalArgumentException("Unknown order status: " + text);
        }
    }
}