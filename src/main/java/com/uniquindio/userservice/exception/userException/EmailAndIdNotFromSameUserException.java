package com.uniquindio.userservice.exception.userException;

public class EmailAndIdNotFromSameUserException extends RuntimeException {
    public EmailAndIdNotFromSameUserException(String message) {
        super(message);
    }
}
