package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * DTO de respuesta que representa la información de un código OTP generado.
 *
 * Incluye identificador, valor del OTP, usuario asociado, fecha de creación y estado.
 */
@Schema(description = "Respuesta con la información del código OTP generado")
public record OtpResponse(

        @Schema(
                description = "Identificador único del OTP en la base de datos",
                example = "101"
        )
        int id,

        @Schema(
                description = "Código OTP generado de 6 dígitos",
                example = "482915"
        )
        @NotNull(message = "El otp es obligatorio")
        @Size(min = 6, max = 6, message = "El otp debe tener 6 dígitos")
        String otp,

        @Schema(
                description = "ID del usuario al que pertenece el OTP",
                example = "42"
        )
        @NotNull(message = "El ID de usuario es obligatorio")
        int user_id,

        @Schema(
                description = "Fecha y hora en que se creó el OTP",
                example = "2025-09-04T14:32:00.000+00:00"
        )
        @NotNull(message = "La fecha de creación es obligatoria")
        Timestamp created_at,

        @Schema(
                description = "Estado actual del OTP (ejemplo: PENDING, USED, EXPIRED)",
                example = "PENDING"
        )
        @NotNull(message = "El estado del OTP es obligatorio")
        String otp_status,

        @Schema(
                description = "URL de recuperacion de contraseña",
                example = "http://local-host:8080/api/v1/users/{id}/password"
        )
        @NotNull(message = "El url de recuperación de contraseña es obligatorio")
                String url
) {
}
