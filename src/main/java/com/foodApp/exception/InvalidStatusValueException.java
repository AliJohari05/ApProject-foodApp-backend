package com.foodApp.exception;

public class InvalidStatusValueException extends IllegalArgumentException {

    public InvalidStatusValueException(String message) {
        super(message);
    }

    public InvalidStatusValueException(String message, Throwable cause) {
        super(message, cause);
    }
}