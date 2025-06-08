package com.foodApp.util;

public enum Message {
    METHOD_NOT_ALLOWED("Method Not Allowed"),
    MISSING_FIELDS("Missing required fields"),
    SERVER_ERROR("Internal Server Error"),
    SIGNUP_SUCCESS("User registered successfully"),
    LOGIN_FAILED("Invalid phone or password"),
    LOGIN_SUCCESS("User logged in successfully"),
    RESTAURANT_REGISTERED("Restaurant created successfully"),
    PHONE_ALREADY_EXIST("Phone number already exists"),
    INVALID_INPUT("Invalid input"),
    UNAUTHORIZED("Unauthorized request"),
    USER_NOT_FOUND("User Not Found"),
    PROFILE_UPDATED("Profile updated successfully"),
    UNSUPPORTED_MEDIA_TYPE("Unsupported media type"),
    FORBIDDEN("Forbidden request"),
    LOGOUT_SUCCESS("User logged out successfully"),
    ERROR_404("Resource not found"),
    STATUS_UPDATED("Status updated"),
    WALLET_TOPPED_UP("Wallet topped up successfully"),
    PAYMENT_SUCCESS("Payment successful"),
    Delivery_ALREDY_ASSIGNED("Delivery already assigned"),
    CHANGED_STATUS_SUCCESS("Changed status successfully");



    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
