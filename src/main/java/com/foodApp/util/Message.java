package com.foodApp.util;

public enum Message {
    METHOD_NOT_ALLOWED("Method Not Allowed"),
    MISSING_FIELDS("Missing required fields"),
    SERVER_ERROR("Internal Server Error"),
    SIGNUP_SUCCESS("User registered successfully"),
    LOGIN_FAILED("Login failed"),
    RESTAURANT_REGISTERED("Restaurant registered successfully (pending approval)");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
