package com.uniquindio.userservice.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO que representa una respuesta paginada de usuarios.
 *
 * Contiene información de paginación (total de elementos, páginas, etc.)
 * y la lista de usuarios correspondientes a la página solicitada.
 */
@Schema(description = "Respuesta paginada con metadatos y lista de usuarios")
public record PaginatedUserResponse(

        @Schema(
                description = "Número total de usuarios disponibles en el sistema",
                example = "125"
        )
        int totalItems,

        @Schema(
                description = "Número total de páginas calculadas según el tamaño de página",
                example = "13"
        )
        int totalPages,

        @Schema(
                description = "Número de la página actual (comienza en 1)",
                example = "2"
        )
        int currentPage,

        @Schema(
                description = "Cantidad de elementos devueltos por página",
                example = "10"
        )
        int pageSize,

        @ArraySchema(
                schema = @Schema(implementation = UserResponse.class),
                arraySchema = @Schema(description = "Lista de usuarios de la página actual")
        )
        List<UserResponse> users
) {
}
