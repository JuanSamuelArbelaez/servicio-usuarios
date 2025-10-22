Feature: Generación de OTP

  Scenario: Generar OTP exitosamente (camino feliz)
    Given existe un usuario registrado
    When envío POST a "/api/v1/auth/otp" con:
      | email | usuario@ejemplo.com |
    Then la respuesta debe tener código 200
    And debe mostrar "OTP sent successfully"

  Scenario: Usuario no registrado
    When envío POST con email "noexiste@ejemplo.com"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"

  Scenario: Email vacío
    When envío POST con email vacío
    Then la respuesta debe tener código 400
    And debe mostrar "Email is required"


Feature: Inicio de sesión de usuario

  Scenario: Iniciar sesión correctamente (camino feliz)
    Given existe un usuario con credenciales válidas
    When envío POST a "/api/v1/auth/login" con:
      | email               | password      |
      | usuario@ejemplo.com | Passw0rd2025  |
    Then la respuesta debe tener código 200
    And debe incluir un token de autenticación

  Scenario: Contraseña incorrecta
    When envío POST con contraseña errónea
    Then la respuesta debe tener código 401
    And debe mostrar "Invalid credentials"

  Scenario: Usuario no registrado
    When envío POST con email "noexiste@ejemplo.com"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"
