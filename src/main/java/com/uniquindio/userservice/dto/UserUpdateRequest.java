package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para actualizar la información de un usuario existente.
 *
 * Solo permite modificar el email y el nombre del usuario.
 */
@Schema(description = "Solicitud para actualizar la información de un usuario")
public record UserUpdateRequest(

        @Schema(
                description = "Nuevo correo electrónico del usuario",
                example = "usuario_actualizado@ejemplo.com"
        )
        @Email(message = "El email debe tener un formato correcto")
        @Size(min = 8, max = 50, message = "El email debe contener entre 8 y 50 caracteres")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(
                description = "Nuevo nombre completo del usuario",
                example = "Andrés Rendón Actualizado"
        )
        @Size(min = 2, max = 50, message = "El nombre debe contener entre 2 y 50 caracteres")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Size(max = 20, message = "El teléfono debe contener máximo 20 caracteres")
        @NotBlank(message = "El teléfono es obligatorio")
        String phone
) {
}
