package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de respuesta que representa a un usuario del sistema.
 *
 * Contiene la información básica de identificación del usuario.
 */
@Schema(description = "Respuesta con la información básica de un usuario")
public record UserResponse(

        @Schema(
                description = "Identificador único del usuario",
                example = "101"
        )
        int id,

        @Schema(
                description = "Nombre completo del usuario",
                example = "Andrés Felipe Rendón"
        )
        String name,

        @Schema(
                description = "Correo electrónico del usuario",
                example = "usuario@ejemplo.com"
        )
        String email,

        @Schema(
                description = "Telefono del usuario",
                example = "3001114444"
        )
        String phone,

        @Schema(
                description = "Estado de cuenta del usuario",
        example = "VERIFIED")
        UserAccountStatusEnum account_status
) {
}
