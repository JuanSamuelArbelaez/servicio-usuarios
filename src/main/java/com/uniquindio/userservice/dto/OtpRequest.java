package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar un código OTP (One-Time Password).
 *
 * El usuario debe proporcionar un correo electrónico válido al que se enviará el OTP.
 */
@Schema(description = "Solicitud de generación/envío de un código OTP a un correo electrónico")
public record OtpRequest(

        @Schema(
                description = "Correo electrónico al cual se enviará el código OTP",
                example = "usuario@ejemplo.com"
        )
        @Email(message = "El email debe tener un formato correcto")
        @Size(min = 8, max = 50, message = "El email debe tener un formato correcto")
        @NotBlank(message = "El email es obligatorio")
        String email
) {
}
