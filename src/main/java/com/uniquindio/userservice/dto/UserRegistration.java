package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO utilizado para registrar un nuevo usuario en el sistema.
 * Contiene los datos necesarios para crear la cuenta.
 */
@Schema(description = "Solicitud para registrar un nuevo usuario en el sistema")
public record UserRegistration(

        @Schema(
                description = "Correo electrónico único del usuario",
                example = "usuario@ejemplo.com"
        )
        @Email(message = "El email debe tener un formato correcto")
        @Size(min = 8, max = 50, message = "El email debe contener entre 8 y 50 caracteres")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(
                description = "Contraseña del usuario (debe incluir mayúscula, minúscula y un dígito)",
                example = "Passw0rd2025"
        )
        @Size(min = 8, max = 50, message = "La contraseña debe contener entre 8 y 50 caracteres")
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$",
                message = "La contraseña debe contener al menos un dígito, una mayúscula y una minúscula"
        )
        String password,

        @Schema(
                description = "Nombre completo del usuario",
                example = "Andrés Felipe Rendón"
        )
        @Size(min = 8, max = 50, message = "El nombre debe contener entre 8 y 50 caracteres")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Size(max = 20, message = "El teléfono debe contener máximo 20 caracteres")
        @NotBlank(message = "El teléfono es obligatorio")
        String phone
) {}
