package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la recuperación de contraseña mediante un código OTP.
 *
 * El usuario debe proporcionar su correo electrónico, un OTP válido recibido por email
 * y la nueva contraseña que desea establecer.
 */
@Schema(description = "Solicitud para recuperar contraseña mediante OTP")
public record PasswordRecoveryRequest(

        @Schema(
                description = "Correo electrónico del usuario asociado a la cuenta",
                example = "usuario@ejemplo.com"
        )
        @Email(message = "El email debe tener un formato correcto")
        @Size(min = 8, max = 50, message = "El email debe tener entre 8 y 50 caracteres")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(
                description = "Código OTP de 6 dígitos enviado al correo electrónico",
                example = "493820"
        )
        @NotNull(message = "El OTP es obligatorio")
        @Min(value = 100000, message = "El OTP debe tener al menos 6 dígitos")
        @Max(value = 999999, message = "El OTP debe tener como máximo 6 dígitos")
        String otp,

        @Schema(
                description = "Nueva contraseña que debe cumplir las reglas de seguridad",
                example = "Nuev0Pass2025"
        )
        @Size(min = 8, max = 50, message = "La contraseña debe contener entre 8 y 50 caracteres")
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$",
                message = "La contraseña debe contener al menos un dígito, una mayúscula y una minúscula"
        )
        String password
) {
}
