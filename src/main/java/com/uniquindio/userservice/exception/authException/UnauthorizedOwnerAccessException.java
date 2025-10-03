package com.uniquindio.userservice.exception.authException;

public class UnauthorizedOwnerAccessException extends RuntimeException {
    public UnauthorizedOwnerAccessException(String message) {
        super(message);
    }
}
