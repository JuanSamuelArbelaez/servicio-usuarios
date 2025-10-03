package com.uniquindio.userservice.service.impl;

import com.uniquindio.userservice.client.AuthClient;
import com.uniquindio.userservice.client.UserClient;
import com.uniquindio.userservice.client.UserNotificationProducer;
import com.uniquindio.userservice.dto.*;
import com.uniquindio.userservice.exception.InvalidOTPException;
import com.uniquindio.userservice.exception.userException.ExternalServiceException;
import com.uniquindio.userservice.exception.userException.IncorrectPasswordException;
import com.uniquindio.userservice.exception.userException.UserNotFoundException;
import com.uniquindio.userservice.exception.OtpCreationException; // Excepción específica que falta
import com.uniquindio.userservice.service.interfaces.AuthService;
import com.uniquindio.userservice.util.JwtUtils;
import com.uniquindio.userservice.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.sql.Timestamp;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final AuthClient authClient;
    private final JwtUtils jwtUtils;
    private final UserNotificationProducer userNotificationProducer;


    @Override
    public String login(LoginRequest loginRequest) {
        log.info("Intentando iniciar sesión para el usuario con email: {}", loginRequest.email());

        try {
            // Buscar usuario en el user-service
            UserAuthResponse user = userClient.getUserByEmail(loginRequest.email());

            // Validar contraseña en este microservicio
            if (!PasswordUtils.matches(loginRequest.password(), user.password())) {
                log.error("Contraseña incorrecta para el usuario {}", loginRequest.email());
                throw new IncorrectPasswordException("Contraseña incorrecta para el usuario " + loginRequest.email());
            }

            // Generar token JWT
            String token = jwtUtils.generateToken(user);
            userNotificationProducer.sendUserLogin(user);

            log.info("Token JWT generado exitosamente para el usuario {}", loginRequest.email());

            return token;

        } catch (WebClientResponseException e) {
            log.error("Error al obtener usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con email " + loginRequest.email() + " no encontrado");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }

    /**
     * Solicita un otp de recuperación de contraseña para el usuario.
     *
     * <p>Este método implementa la lógica de negocio para la solicitud de un otp para recuperación de contraseñas,
     * incluyendo validación del email antes de realizar la operación en el servicio externo.</p>
     *
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El email del usuario debe ser una cadena de texto con formato de correo xxx@dominio.xxx</li>
     * </ul>
     *
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>409 (Conflict):</strong> Se lanza {@link OtpCreationException} si ya existe un código otp activo para el usuario</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     *
     *
     * @param otpRequest Dto que contiene el email de la cuenta que quiere solicitar un otp
     * @return {@link OtpResponse} con la información del otp creado exitosamente
     * @throws UserNotFoundException si el usuario con el email especificado no existe
     * @throws OtpCreationException si gay un error al solicitar el otp
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see AuthClient#requestOtp(OtpRequest)
     */
    @Override
    public OtpResponse requestOtp(OtpRequest otpRequest) {
        String email = otpRequest.email();
        try {
            log.info("Intentando encontrar el usuario con email: {}", email);
            UserAuthResponse user = userClient.getUserByEmail(email);

            log.info("Intentando crear un OTP para el usuario con id: {}", user.id());

            OtpResponse otp = authClient.requestOtp(otpRequest);

            if (Objects.equals(otp.otp_status(), "CREATED")) {
                log.info("Creación de OTP exitosa: {}",otp.otp());

                log.info("El OTP expirará en 5 minutos");
            } else {
                log.error("Fallo al crear el OTP para el usuario {}: Estado: {}", user.id(), otp.otp_status());
                throw new OtpCreationException("Fallo al crear el OTP.");
            }
            userNotificationProducer.sendRequestOtp(user, otp);
            return otp;

        } catch (WebClientResponseException e) {
            log.error("Error al generar el otp. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con email " + email + " no encontrado.");
            }
            if (e.getStatusCode().value() == 409) {
                throw new OtpCreationException("Error al generar el OTP.");
            }
            throw new ExternalServiceException(
                    "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
            );
        }
    }
}
