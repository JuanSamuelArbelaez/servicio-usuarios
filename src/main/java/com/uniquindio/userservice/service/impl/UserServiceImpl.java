package com.uniquindio.userservice.service.impl;

import com.uniquindio.userservice.annotation.IsOwner;
import com.uniquindio.userservice.client.UserClient;
import com.uniquindio.userservice.client.UserNotificationProducer;
import com.uniquindio.userservice.dto.*;
import com.uniquindio.userservice.exception.InvalidOTPException;
import com.uniquindio.userservice.exception.OtpCreationException;
import com.uniquindio.userservice.exception.userException.*;
import com.uniquindio.userservice.service.interfaces.UserService;
import com.uniquindio.userservice.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Implementación del servicio de gestión de usuarios.
 * 
 * <p>Esta clase implementa la lógica de negocio para la gestión de usuarios, actuando como
 * una capa intermedia entre el controlador y el cliente HTTP que se comunica con el servicio
 * externo de usuarios. Proporciona funcionalidades CRUD completas con manejo robusto de
 * errores y validaciones de negocio.</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Encriptación automática de contraseñas antes de enviarlas al servicio externo</li>
 *   <li>Manejo granular de errores HTTP con excepciones específicas del dominio</li>
 *   <li>Validación de IDs de usuario para prevenir operaciones con identificadores inválidos</li>
 *   <li>Logging detallado para auditoría y debugging de operaciones</li>
 *   <li>Traducción de códigos de error HTTP a excepciones de negocio específicas</li>
 * </ul>
 * 
 * <p><strong>Manejo de errores:</strong> El servicio categoriza los errores HTTP en
 * excepciones específicas del dominio, facilitando el manejo apropiado en las capas
 * superiores de la aplicación.</p>
 * 
 * <p><strong>Seguridad:</strong> Las contraseñas se encriptan automáticamente antes
 * de ser enviadas al servicio externo, utilizando la utilidad {@link PasswordUtils}.</p>
 * 
 * @author Andres Felipe Rendon
 * @version 1.0.0
 * @see UserService
 * @see UserClient
 * @see PasswordUtils
 * @see DuplicateEmailException
 * @see ExternalServiceException
 * @see InvalidIdException
 * @see UserNotFoundException
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /**
     * Cliente HTTP para la comunicación con el servicio externo de usuarios.
     */
    private final UserClient userClient;
    private final UserNotificationProducer userNotificationProducer;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Este método implementa la lógica de negocio para el registro de usuarios,
     * incluyendo la encriptación automática de la contraseña antes de enviarla al
     * servicio externo. El proceso incluye validaciones y manejo robusto de errores.</p>
     * 
     * <p><strong>Flujo de la operación:</strong></p>
     * <ol>
     *   <li>Encriptación de la contraseña del usuario usando {@link PasswordUtils}</li>
     *   <li>Intento de registro en el servicio externo mediante {@link UserClient}</li>
     *   <li>Manejo de respuestas exitosas y errores HTTP</li>
     *   <li>Traducción de códigos de error a excepciones específicas del dominio</li>
     * </ol>
     *
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>409 (Conflict):</strong> Se lanza {@link DuplicateEmailException} si el email ya existe</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     * 
     * <p><strong>Seguridad:</strong> La contraseña se encripta antes de ser enviada
     * al servicio externo para proteger la información sensible del usuario.</p>
     *
     * @param user DTO con la información del usuario a registrar
     * @return {@link UserResponse} con la información del usuario registrado exitosamente
     * @throws DuplicateEmailException si el correo electrónico ya está registrado en el sistema
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserRegistration
     * @see UserResponse
     * @see PasswordUtils#encryptPassword(UserRegistration)
     */
    @Override
    public UserResponse registerUser(UserRegistration user) {
        UserRegistration encryptedUser = PasswordUtils.encryptPassword(user);

        try {
            log.info("Intentando registrar usuario con email: {}", encryptedUser.email());
            UserResponse response = userClient.registerUser(encryptedUser);
            log.info("Usuario registrado exitosamente con id: {}", response.id());
            userNotificationProducer.sendWelcome(response);
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error al registrar usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 409) {
                throw new DuplicateEmailException("El correo electrónico ya está registrado.");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }

    /**
     * Obtiene una lista paginada de usuarios del sistema.
     * 
     * <p>Este método recupera usuarios de forma paginada desde el servicio externo,
     * proporcionando funcionalidad para aplicaciones que manejan grandes volúmenes
     * de usuarios. La paginación se delega completamente al servicio externo.</p>
     * 
      * <p><strong>Características de la paginación:</strong></p>
 * <ul>
 *   <li>Los parámetros de paginación se pasan directamente al servicio externo</li>
 *   <li>La respuesta incluye metadatos de paginación (total de elementos, página actual, etc.)</li>
 *   <li>El servicio externo es responsable de implementar la lógica de paginación</li>
 * </ul>
     * 
     * <p><strong>Manejo de errores:</strong> Cualquier error HTTP del servicio externo
     * se traduce a {@link ExternalServiceException} con detalles del error original.</p>
     * 
     * <p><strong>Logging:</strong> Se registra información detallada sobre la operación,
     * incluyendo el número total de usuarios recuperados en la página solicitada.</p>
     *
     * @param page Número de página a consultar (comienza en 1)
     * @param size Tamaño de la página (número de elementos por página)
     * @return {@link PaginatedUserResponse} con la lista paginada de usuarios y metadatos
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see PaginatedUserResponse
     * @see UserClient#getUsersPaginated(int, int)
     */
    @Override
    public PaginatedUserResponse getUsers(int page, int size) {
        try {
            log.info("Obteniendo usuarios, página: {}, tamaño: {}", page, size);
            PaginatedUserResponse response = userClient.getUsersPaginated(page, size);
            log.info("Usuarios obtenidos exitosamente, total en página: {}",
                    response != null ? response.users().size() : 0);
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error al obtener usuarios. Código: {}, Detalle: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException(
                    "Error inesperado al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString());
        }
    }

    /**
     * Obtiene la información detallada de un usuario específico por su identificador.
     * 
     * <p>Este método implementa la lógica de negocio para consultar usuarios individuales,
     * incluyendo validación del ID antes de realizar la consulta al servicio externo.
     * Proporciona manejo robusto de errores con excepciones específicas del dominio.</p>
     * 
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El ID del usuario debe ser un entero positivo mayor que 0</li>
     *   <li>Se lanza {@link InvalidIdException} si el ID no cumple con los criterios</li>
     * </ul>
     * 
     * <p><strong>Manejo de errores:</strong>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul></p>
     * 
     * <p><strong>Logging:</strong> Se registra información detallada sobre la operación,
     * incluyendo el ID del usuario consultado y el resultado de la operación.</p>
     *
     * @param userId Identificador único del usuario a consultar
     * @return {@link UserResponse} con la información completa del usuario solicitado
     * @throws InvalidIdException si el ID proporcionado no es válido (≤ 0)
     * @throws UserNotFoundException si el usuario con el ID especificado no existe
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserResponse
     * @see UserClient#getUserById(int)
     * @see #validateUserId(int)
     */
    @Override
    public UserResponse getUser(int userId) {
        validateUserId(userId);
        try {
            log.info("Intentando obtener usuario con id: {}", userId);
            UserResponse response = userClient.getUserById(userId);
            log.info("Usuario obtenido exitosamente con id: {}", response.id());
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error al obtener usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con id " + userId + " no encontrado.");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }

    /**
     * Actualiza la información de un usuario existente en el sistema.
     * 
     * <p>Este método implementa la lógica de negocio para la actualización de usuarios,
     * incluyendo validación del ID antes de realizar la operación en el servicio externo.
     * La actualización es idempotente y solo modifica los campos proporcionados.</p>
     * 
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El ID del usuario debe ser un entero positivo mayor que 0</li>
     *   <li>Se lanza {@link InvalidIdException} si el ID no cumple con los criterios</li>
     * </ul>
     * 
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>406 (Not Verified):</strong> Se lanza {@link UserAccountNotVerifiedException} si el usuario con el correo provisto no está verificado</li>    *
     *   <li><strong>409 (Conflict):</strong> Se lanza {@link DuplicateEmailException} si el nuevo email ya existe</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     * 
     * <p><strong>Logging:</strong> Se registra información detallada sobre la operación,
     * incluyendo el ID del usuario actualizado y el resultado de la operación.</p>
     *
     * @param id Identificador único del usuario a actualizar
     * @param userUpdateRequest DTO con los datos actualizados del usuario
     * @return {@link UserResponse} con la información actualizada del usuario
     * @throws InvalidIdException si el ID proporcionado no es válido (≤ 0)
     * @throws UserNotFoundException si el usuario con el ID especificado no existe
     * @throws UserAccountNotVerifiedException si el usuario con el correo provisto no está verificado
     * @throws DuplicateEmailException si el nuevo correo electrónico ya está registrado
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserUpdateRequest
     * @see UserResponse
     * @see UserClient#updateUser(int, UserUpdateRequest)
     * @see #validateUserId(int)
     */
    @Override
    @IsOwner
    public UserResponse updateUser(int id, UserUpdateRequest userUpdateRequest) {
        validateUserId(id);
        String email = userUpdateRequest.email();
        try {
            UserResponse response = userClient.getUserById(id);
            if(response.account_status()!=UserAccountStatusEnum.VERIFIED){
                log.error("Error en actualización de contraseña: Usuario con el email {}, con estado de cuenta {} (Debe ser {})", email, response.account_status(), UserAccountStatusEnum.VERIFIED);
                throw new UserAccountNotVerifiedException("Usuario con email: "+ email + " no está verificado. Por favor verificar.");
            }

            log.info("Intentando actualizar usuario con id: {}", id);
            response = userClient.updateUser(id, userUpdateRequest);
            log.info("Usuario actualizado exitosamente con id: {}", response.id());
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error al actualizar usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con id " + id + " no encontrado.");
            }
            else if (e.getStatusCode().value() == 406) {
                throw new UserAccountNotVerifiedException("Usuario con email " + email + " no verificado.");
            }
            else if (e.getStatusCode().value() == 409) {
                throw new DuplicateEmailException("El correo electrónico ya está registrado.");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }

    /**
     * Elimina lógicamente un usuario del sistema.
     * 
     * <p>Este método implementa la lógica de negocio para la eliminación de usuarios,
     * incluyendo validación del ID antes de realizar la operación en el servicio externo.
     * La eliminación es lógica, lo que significa que los datos no se eliminan físicamente
     * de la base de datos del servicio externo.</p>
     * 
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El ID del usuario debe ser un entero positivo mayor que 0</li>
     *   <li>Se lanza {@link InvalidIdException} si el ID no cumple con los criterios</li>
     * </ul>
     * 
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     * 
     * <p><strong>Logging:</strong> Se registra información detallada sobre la operación,
     * incluyendo el ID del usuario eliminado y el resultado de la operación.</p>
     * 
     * <p><strong>Nota de seguridad:</strong> La eliminación lógica permite la recuperación
     * de cuentas si es necesario, pero puede requerir permisos especiales dependiendo
     * de la implementación del servicio externo.</p>
     *
     * @param userId Identificador único del usuario a eliminar
     * @throws InvalidIdException si el ID proporcionado no es válido (≤ 0)
     * @throws UserNotFoundException si el usuario con el ID especificado no existe
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserClient#deleteUser(int)
     * @see #validateUserId(int)
     */
    @Override
    @IsOwner
    public void deleteUser(int userId) {
        validateUserId(userId);
        try {
            log.info("Intentando eliminar usuario con id: {}", userId);
            userClient.deleteUser(userId);
            log.info("Usuario con id {} eliminado exitosamente.", userId);
        } catch (WebClientResponseException e) {
            log.error("Error al eliminar usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con id " + userId + " no encontrado.");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }

    /**
     * Valida que el ID proporcionado sea un entero positivo.
     * 
     * <p>Este método de utilidad valida que el ID del usuario sea válido antes de
     * realizar operaciones en el servicio externo. Previene operaciones con
     * identificadores inválidos que podrían causar errores o comportamientos
     * inesperados.</p>
     * 
     * <p><strong>Criterios de validación:</strong></p>
     * <ul>
     *   <li>El ID debe ser mayor que 0</li>
     *   <li>Se aceptan todos los enteros positivos válidos</li>
     * </ul>
     * 
     * <p><strong>Comportamiento en caso de error:</strong> Si la validación falla,
     * se lanza {@link InvalidIdException} con un mensaje descriptivo que incluye
     * el valor del ID que causó el error.</p>
     * 
     * <p><strong>Uso:</strong> Este método se llama automáticamente en todos los
     * métodos públicos que requieren un ID de usuario como parámetro.</p>
     *
     * @param userId ID del usuario a validar
     * @throws InvalidIdException si el ID es menor o igual a 0
     * @see InvalidIdException
     */
    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new InvalidIdException("El ID proporcionado no es válido: " + userId);
        }
    }


    /**
     * Recupera la contraseña del usuario, haciendo uso del otp.
     *
     * <p>Este método implementa la lógica de negocio para la recuperación de contraseñas,
     * incluyendo validación del email antes de realizar la operación en el servicio externo, con el uso del otp.</p>
     *
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El email del usuario debe ser una cadena de texto con formato de correo xxx@dominio.xxx</li>
     *   <li>El otp del usuario debe ser un número de 6 dígitos xxxxxx</li>
     * </ul>
     *
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>405 (Not Match):</strong> Se lanza {@link EmailAndIdNotFromSameUserException} si el usuario con el correo provisto no coincide con el usuario con el id provisto</li>
     *   <li><strong>409 (Conflict):</strong> Se lanza {@link OtpCreationException} si hay un error con el otp</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     *
     *
     * @param passwordRecoveryRequest contiene el email del usuario, y su otp, para re-establecer su contraseña
     * @return {@link Boolean} True si la actualización fue exitosa. False si no.
     * @throws UserNotFoundException si el usuario con el email especificado no existe
     * @throws EmailAndIdNotFromSameUserException si el usuario con el correo provisto no coincide con el usuario con el id provisto
     * @throws OtpCreationException si el otp no se pudo validar
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserClient#recoverPassword(PasswordRecoveryRequest, int)
     */
    @Override
    public boolean updatePassword(PasswordRecoveryRequest passwordRecoveryRequest, int id) {
        String email = passwordRecoveryRequest.email();
        String otp = passwordRecoveryRequest.otp();
        try {
            log.info("Intentando encontrar el usuario con id:{}, e email: {}", id, email);

            UserResponse response = userClient.getUserById(id);
            if (!response.email().equals(email)){
                log.error("Error en actualización de contraseña: Usuario con el email {}, no es el mismo usuario con id {} ", email, id);
                throw  new EmailAndIdNotFromSameUserException("Email "+email +" no corresponde al correo del usuario con id: "+ id);
            }

            log.info("Intentando cambiar la contraseña para el usuario con id: {}, e email: {}, usando el OTP: {}", id, email, otp);
            PasswordRecoveryRequest pr = PasswordUtils.encryptPassword(passwordRecoveryRequest);
            userClient.recoverPassword(pr, id);
            userNotificationProducer.sendPasswordChanged(response);

            return true;

        } catch (WebClientResponseException e) {
            log.error("Error al actualizar la contraseña. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con email " + email + " no encontrado.");
            }
            if (e.getStatusCode().value() == 405) {
                throw new EmailAndIdNotFromSameUserException("Al usuario con id " + id + " no le pertenece el email " + email);
            }
            if (e.getStatusCode().value() == 400){
                throw new InvalidOTPException("El opt es invalido o ha expirado");
            }
            throw new ExternalServiceException(
                    "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
            );
        }
    }

    /**
     * Cambia el estado de un usuario de {@link UserAccountStatusEnum#PENDING_VALIDATION} a {@link UserAccountStatusEnum#VERIFIED}.
     *
     * <p>Este método implementa la lógica de negocio para la activación de usuarios que ya existen en el sistema
     * pero aún no han completado su validación. El cambio solo se realiza si el usuario se encuentra en estado
     * {@code PENDING_VALIDATION}.</p>
     *
     * <p><strong>Validaciones previas:</strong></p>
     * <ul>
     *   <li>El ID del usuario debe ser un entero positivo mayor que 0</li>
     *   <li>El usuario debe existir y tener estado {@code PENDING_VALIDATION}</li>
     *   <li>Se lanza {@link InvalidIdException} si el ID no cumple con los criterios</li>
     *   <li>Se lanza {@link InvalidUserStatusException} si el usuario no está en estado {@code PENDING_VALIDATION}</li>
     * </ul>
     *
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><strong>404 (Not Found):</strong> Se lanza {@link UserNotFoundException} si el usuario no existe</li>
     *   <li><strong>Otros códigos:</strong> Se lanza {@link ExternalServiceException} con detalles del error</li>
     * </ul>
     *
     * <p><strong>Logging:</strong> Se registra información detallada sobre la operación,
     * incluyendo el ID del usuario actualizado y el resultado del cambio de estado.</p>
     *
     * @param userId Identificador único del usuario cuyo estado se actualizará
     * @throws InvalidIdException si el ID proporcionado no es válido (≤ 0)
     * @throws UserNotFoundException si el usuario con el ID especificado no existe
     * @throws InvalidUserStatusException si el usuario no está en estado {@code PENDING_VALIDATION}
     * @throws ExternalServiceException si ocurre un error de comunicación con el servicio externo
     * @see UserClient#verifyUser(int)
     * @see #validateUserId(int)
     */
    @Override
    public AccountStatusResponse verifyUserAccount(int userId) {
        validateUserId(userId);
        try {
            log.info("Iniciando verificación de usuario con id: {}", userId);

            UserResponse response = userClient.getUserById(userId);
            if (response.account_status() == UserAccountStatusEnum.VERIFIED) {
                throw new InvalidUserStatusException("El usuario ya esta verificado.");
            }

            if (response.account_status() == UserAccountStatusEnum.DELETED) {
                throw new InvalidUserStatusException("El usuario se encuentra elimindado.");
            }

            AccountStatusResponse response2 = userClient.verifyUser(userId);
            userNotificationProducer.sendAccountVerified(response);
            log.info("Usuario con id {} verificado exitosamente.", userId);

            return response2;


        } catch (WebClientResponseException e) {
            log.error("Error al verificar usuario. Código: {}, Detalle: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException("Usuario con id " + userId + " no encontrado.");
            } else {
                throw new ExternalServiceException(
                        "Error al comunicarse con el servicio de usuarios: " + e.getResponseBodyAsString()
                );
            }
        }
    }
}
