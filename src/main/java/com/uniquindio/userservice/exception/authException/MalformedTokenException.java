package com.uniquindio.userservice.exception.authException;

public class MalformedTokenException extends RuntimeException {
    public MalformedTokenException(String message) {
        super(message);
    }
}

