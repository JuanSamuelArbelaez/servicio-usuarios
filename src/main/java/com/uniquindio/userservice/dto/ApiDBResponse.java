package com.uniquindio.userservice.dto;

public record ApiDBResponse<T>(
        boolean success,
        String message,
        T data,
        Object error,
        int statusCode,
        String timestamp
) {}

