package com.uniquindio.userservice.dto;

public record UserAuthResponse(
        int id,
        String name,
        String email,
        String phone,
        String password // encriptada en DB

) {
}
