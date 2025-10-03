package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de inicio de sesión.
 *
 * Contiene las credenciales necesarias (email y contraseña) para autenticar un usuario.
 */
@Schema(description = "DTO que representa las credenciales para iniciar sesión en el sistema")
public record LoginRequest(

        @Schema(
                description = "Correo electrónico del usuario",
                example = "usuario@ejemplo.com"
        )
        @NotBlank(message = "El correo electrónico es obligatorio")
        String email,

        @Schema(
                description = "Contraseña de la cuenta del usuario",
                example = "P@ssw0rd123"
        )
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
