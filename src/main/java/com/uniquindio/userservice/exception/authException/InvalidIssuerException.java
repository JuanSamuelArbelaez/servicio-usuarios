package com.uniquindio.userservice.exception.authException;

public class InvalidIssuerException extends RuntimeException {
    public InvalidIssuerException(String message) {
        super(message);
    }
}
