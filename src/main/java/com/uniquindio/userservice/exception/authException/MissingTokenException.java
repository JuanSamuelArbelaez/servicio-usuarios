package com.uniquindio.userservice.exception.authException;


public class MissingTokenException extends RuntimeException {
        public MissingTokenException(String message) {
            super(message);
        }
}

