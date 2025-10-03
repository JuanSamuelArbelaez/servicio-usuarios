package com.uniquindio.userservice.controller;

import com.uniquindio.userservice.dto.*;
import com.uniquindio.userservice.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controlador REST para la gestión de usuarios del sistema.
 *
 * <p>Esta clase expone endpoints HTTP para realizar operaciones CRUD (Create, Read, Update, Delete)
 * sobre usuarios. Implementa las mejores prácticas RESTful incluyendo códigos de estado HTTP
 * apropiados, headers de ubicación para recursos creados, y validación de entrada.</p>
 *
 * <p>El controlador utiliza anotaciones de validación de Jakarta para asegurar la integridad
 * de los datos de entrada y proporciona logging detallado para auditoría y debugging.</p>
 *
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Validación automática de DTOs de entrada</li>
 *   <li>Respuestas HTTP estándar con códigos de estado apropiados</li>
 *   <li>Headers de ubicación para recursos creados/actualizados</li>
 *   <li>Logging estructurado con emojis para mejor legibilidad</li>
 *   <li>Paginación para listas de usuarios</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong> Algunos endpoints pueden requerir autenticación y autorización
 * dependiendo de la implementación del servicio subyacente.</p>
 *
 * @author Andres Felipe Rendon
 * @version 1.0.0
 * @see UserService
 * @see UserRegistration
 * @see UserResponse
 * @see UserUpdateRequest
 * @see PaginatedUserResponse
 * @see ResponseEntity
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios (CRUD, paginación, eliminación lógica)")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema. Retorna el usuario creado junto con la ubicación del recurso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(
            @RequestBody @Valid UserRegistration userRegistration) {

        log.info("Solicitud recibida para registrar usuario: {}", userRegistration.email());
        UserResponse userResponse = userService.registerUser(userRegistration);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.id())
                .toUri();

        log.info("Usuario registrado exitosamente: id={}, email={}", userResponse.id(), userResponse.email());
        return ResponseEntity.created(location).body(userResponse);
    }

    @Operation(
            summary = "Obtener lista paginada de usuarios",
            description = "Consulta los usuarios del sistema con paginación. La numeración de páginas comienza en 1."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    @GetMapping
    public ResponseEntity<PaginatedUserResponse> getUsers(
            @Parameter(description = "Número de página (mínimo 1)", example = "1")
            @RequestParam(defaultValue = "1") @Positive int page,
            @Parameter(description = "Tamaño de la página (mínimo 1, máximo 100)", example = "10")
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("📋 Consultando usuarios - Página: {}, Tamaño: {}", page, size);
        PaginatedUserResponse response = userService.getUsers(page, size);
        log.info("✅ Total de usuarios recuperados: {}", response.totalItems());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar usuario por ID",
            description = "Obtiene los datos de un usuario específico mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "Identificador único del usuario", example = "101")
            @PathVariable int userId) {

        log.info("🔎 Consultando usuario con ID: {}", userId);
        UserResponse response = userService.getUser(userId);
        log.info("✅ Usuario encontrado: {}", response.email());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Modifica los datos de un usuario existente. Solo los campos enviados en la solicitud serán actualizados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "Identificador único del usuario", example = "101")
            @PathVariable int id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {

        log.info("✏️ Actualizando usuario con ID: {}", id);
        UserResponse response = userService.updateUser(id, userUpdateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        log.info("✅ Usuario actualizado: {}", response.email());
        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, location.toString())
                .body(response);
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina lógicamente un usuario del sistema. No borra físicamente los datos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Identificador único del usuario", example = "101")
            @PathVariable int id) {

        log.info("🗑️ Eliminando usuario con ID: {}", id);
        userService.deleteUser(id);
        log.info("✅ Usuario con ID: {} eliminado correctamente", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Válida el OTP y actualiza la contraseña.
     */
    @Operation(
            summary = "Recuperar contraseña",
            description = "Valida el código OTP enviado al correo y permite restablecer la contraseña del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "true"))),
            @ApiResponse(responseCode = "400", description = "OTP inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid PasswordRecoveryRequest request, @PathVariable int  id) {
        log.info("🔑 Recuperación de contraseña solicitada para: {}", id);
        userService.updatePassword(request, id);
        log.info("✅ Contraseña actualizada para: {}", id);
        return ResponseEntity.ok("Contraseña reestablecida para el usuario");
    }

    @PatchMapping("/{id}/account_status")
    @Operation(summary = "Actualizar estado de cuenta", description = "Permite verificar o cambiar el estado de la cuenta de un usuario")
    public ResponseEntity<AccountStatusResponse> updateAccountStatus(
            @PathVariable int id
    ) {
        log.info("🔄 Actualizando estado de cuenta del usuario {} a {}", id, UserAccountStatusEnum.VERIFIED);

        AccountStatusResponse accountStatus = userService.verifyUserAccount(id);

        return ResponseEntity.ok(accountStatus);
    }

}
