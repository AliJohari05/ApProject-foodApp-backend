package com.foodApp.model;

public enum Status {
    APPROVED,
    REJECTED;

    public static boolean isValid(String value) {
        try {
            Status.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
