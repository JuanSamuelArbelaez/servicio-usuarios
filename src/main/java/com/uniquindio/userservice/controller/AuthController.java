package com.uniquindio.userservice.controller;

import com.uniquindio.userservice.dto.*;
import com.uniquindio.userservice.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la autenticación y gestión de credenciales.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para login, OTP y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión y retorna el JWT.
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Permite autenticar un usuario con email y contraseña. Devuelve un token JWT si las credenciales son correctas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("🔐 Login solicitado para: {}", loginRequest.email());
        String token = authService.login(loginRequest);
        log.info("✅ Login exitoso para: {}", loginRequest.email());
        return ResponseEntity.ok(token);
    }

    /**
     * Genera/solicita un OTP para el email indicado.
     */
    @Operation(
            summary = "Generar OTP",
            description = "Genera un código de un solo uso (OTP) y lo envía al correo del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP generado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OtpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/otp")
    public ResponseEntity<OtpResponse> requestOtp(@RequestBody @Valid OtpRequest request) {
        log.info("📩 Solicitud de OTP para: {}", request.email());
        OtpResponse otp = authService.requestOtp(request);
        log.info("✅ OTP generado para: {}.", request.email());
        return ResponseEntity.ok(otp);
    }
}
