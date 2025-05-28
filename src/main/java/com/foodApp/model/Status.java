package com.foodApp.model;

import com.foodApp.exception.InvalidStatusValueException;

public enum Status {
    APPROVED,
    REJECTED,
    PENDING_APPROVAL;

    public static boolean isValid(String value) {
        try {
            Status.valueOf(value.toUpperCase());
            return true;
        } catch (InvalidStatusValueException e) {
            throw new InvalidStatusValueException(e.getMessage(),e);
        }
    }
}