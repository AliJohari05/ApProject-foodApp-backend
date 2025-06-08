package com.foodApp.exception;

public class InvalidDeliveryStatusTransitionException extends RuntimeException {
    public InvalidDeliveryStatusTransitionException(String message) {
        super(message);
    }
}