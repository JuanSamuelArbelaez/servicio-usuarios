package com.uniquindio.userservice.client;

import com.uniquindio.userservice.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Cliente HTTP reactivo para la comunicación con el servicio de usuarios.
 * 
 * <p>Esta clase proporciona métodos para realizar operaciones CRUD (Create, Read, Update, Delete)
 * sobre usuarios mediante llamadas HTTP al servicio de usuarios. Utiliza WebClient de Spring
 * WebFlux para operaciones reactivas y no bloqueantes.</p>
 * 
 * <p>La clase está diseñada para trabajar con respuestas tipadas que siguen el patrón
 * {@link ApiDBResponse} para mantener consistencia en la estructura de respuesta.</p>
 * 
 * <p><strong>Nota de configuración:</strong> La URL base del servicio se configura a través
 * de variables de entorno. Para desarrollo local, se utiliza {@code localhost:8082}, pero
 * en producción debe configurarse mediante la variable de entorno {@code DATA_SERVICE_URL}.</p>
 * 
 * @author Andres Felipe Rendon
 * @version 1.0.0
 * @see WebClient
 * @see ApiDBResponse
 * @see UserResponse
 * @see UserRegistration
 * @see UserUpdateRequest
 * @see PaginatedUserResponse
 */
@Component
public class UserClient {

    /**
     * Cliente HTTP reactivo para realizar las peticiones al servicio de usuarios.
     */
    private final WebClient webClient;

    /**
     * Constructor que inicializa el cliente HTTP con la URL base del servicio.
     * 
     * <p>La URL base se obtiene de la variable de entorno {@code DATA_SERVICE_URL}.
     * Si no está definida, se utiliza {@code http://localhost:8082/api/users} como valor por defecto.</p>
     * 
     * @param builder Constructor de WebClient proporcionado por Spring
     * @see WebClient.Builder
     */
    public UserClient(WebClient.Builder builder) {
        String baseUrl = System.getenv("DATA_SERVICE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8082/api/users"; // Valor por defecto para desarrollo
        }
        
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Este método envía una petición POST al endpoint {@code /register} del servicio
     * de usuarios con los datos de registro proporcionados.</p>
     * 
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente. En aplicaciones completamente
     * reactivas, considere usar la versión no bloqueante.</p>
     * 
     * @param userRequest DTO con los datos del usuario a registrar
     * @return {@link UserResponse} con la información del usuario registrado, o {@code null}
     *         si la operación falla o no hay respuesta
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see UserRegistration
     * @see UserResponse
     */
    public UserResponse registerUser(UserRegistration userRequest) {
        ApiDBResponse<UserResponse> response = webClient.post()
                .uri("/register")
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<UserResponse>>() {
                })
                .block();

        return response != null ? response.data() : null;
    }

    /**
     * Obtiene una lista paginada de usuarios del sistema.
     * 
     * <p>Este método envía una petición GET al endpoint base del servicio de usuarios
     * con parámetros de consulta para paginación.</p>
     * 
      * <p><strong>Parámetros de paginación:</strong></p>
 * <ul>
 *   <li>{@code page}: Número de página (comienza en 0)</li>
 *   <li>{@code size}: Tamaño de la página (número de elementos por página)</li>
 * </ul>
     * 
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     * 
     * @param page Número de página a consultar (0-based)
     * @param size Tamaño de la página (número de elementos por página)
     * @return {@link PaginatedUserResponse} con la lista paginada de usuarios, o {@code null}
     *         si la operación falla o no hay respuesta
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see PaginatedUserResponse
     */
    public PaginatedUserResponse getUsersPaginated(int page, int size) {
        ApiDBResponse<PaginatedUserResponse> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<PaginatedUserResponse>>() {})
                .block();

        return response != null ? response.data() : null;
    }

    /**
     * Obtiene un usuario específico por su identificador único.
     * 
     * <p>Este método envía una petición GET al endpoint {@code /{id}} del servicio
     * de usuarios para obtener la información completa de un usuario específico.</p>
     * 
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     * 
     * @param id Identificador único del usuario a consultar
     * @return {@link UserResponse} con la información del usuario, o {@code null}
     *         si el usuario no existe o la operación falla
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see UserResponse
     */
    public UserResponse getUserById(int id) {
        ApiDBResponse<UserResponse> response = webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<UserResponse>>() {})
                .block();

        return response != null ? response.data() : null;
    }

    /**
     * Actualiza la información de un usuario existente en el sistema.
     * 
     * <p>Este método envía una petición PUT al endpoint {@code /{id}} del servicio
     * de usuarios con los datos actualizados del usuario.</p>
     * 
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     * 
     * @param userId Identificador único del usuario a actualizar
     * @param userUpdate DTO con los datos actualizados del usuario
     * @return {@link UserResponse} con la información actualizada del usuario, o {@code null}
     *         si la operación falla o no hay respuesta
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see UserUpdateRequest
     * @see UserResponse
     */
    public UserResponse updateUser(int userId, UserUpdateRequest userUpdate) {
        ApiDBResponse<UserResponse> response = webClient.put()
                .uri("/{id}", userId)
                .bodyValue(userUpdate)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<UserResponse>>() {})
                .block();

        return response != null ? response.data() : null;
    }

    /**
     * Elimina un usuario del sistema por su identificador único.
     * 
     * <p>Este método envía una petición DELETE al endpoint {@code /{id}} del servicio
     * de usuarios para eliminar permanentemente un usuario.</p>
     * 
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     * 
     * <p><strong>Advertencia:</strong> Esta operación es irreversible. Una vez eliminado,
     * el usuario no puede ser recuperado.</p>
     * 
     * @param id Identificador único del usuario a eliminar
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     *         o si el usuario no existe
     */
    public void deleteUser(int id) {
        webClient.delete()
                .uri("/{id}", id)
                .retrieve()
                .toBodilessEntity()
                .block();
    }



    /**
     * Obtiene un usuario específico por su identificador único.
     *
     * <p>Este método envía una petición GET al endpoint {@code /{id}} del servicio
     * de usuarios para obtener la información completa de un usuario específico.</p>
     *
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     *
     * @param email único del usuario a consultar
     * @return {@link UserResponse} con la información del usuario, o {@code null}
     *         si el usuario no existe o la operación falla
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see UserResponse
     */
    public UserAuthResponse getUserByEmail(String email) {
        ApiDBResponse<UserAuthResponse> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/email")
                        .queryParam("value", email) // aquí pasamos el email como query param
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<UserAuthResponse>>() {})
                .block();

        return response != null ? response.data() : null;
    }

    /**
     * Cambia la contraseña de un usuario
     *
     * <p>Este método envía una petición PATCH al endpoint {@code /{id}/password} del servicio
     * de usuarios para procesar la solicitud de cambio de contraseña.</p>
     *
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     *
     * @param recoveryRequest solicitud con el correo y otp del usuario a consultar
     * @param id único del usuario a consultar. Si el usuario no existe o la operación falla
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     * @see UserResponse
     */
    public void recoverPassword(PasswordRecoveryRequest recoveryRequest, int id) {
        webClient.patch()
                .uri("/{id}/password", id)
                .bodyValue(recoveryRequest)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /**
     * Elimina un usuario del sistema por su identificador único.
     *
     * <p>Este método envía una petición DELETE al endpoint {@code /{id}} del servicio
     * de usuarios para eliminar permanentemente un usuario.</p>
     *
     * <p><strong>Nota:</strong> Este método es bloqueante (usa {@code .block()}) para
     * mantener compatibilidad con código síncrono existente.</p>
     *
     * <p><strong>Advertencia:</strong> Esta operación es irreversible. Una vez eliminado,
     * el usuario no puede ser recuperado.</p>
     *
     * @param id Identificador único del usuario a eliminar
     * @throws WebClientResponseException si ocurre un error en la comunicación HTTP
     *         o si el usuario no existe
     */
    public AccountStatusResponse verifyUser(int id) {
        ApiDBResponse<AccountStatusResponse> response =webClient.patch()
                .uri("/{id}/account_status", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiDBResponse<AccountStatusResponse>>() {})
                .block();

        return response != null ? response.data() : null;
    }
}

