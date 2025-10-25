Feature: Registro de usuarios

  Scenario: Registrar usuario con datos válidos (camino feliz)
    Given la API está disponible
    When envío una solicitud POST a "/api/v1/users" con:
      | email               | password      | name                  | phone      |
      | usuario@ejemplo.com | Passw0rd2025  | Andrés Rendón         | 3101110000 |
    Then la respuesta debe tener código 201
    And el cuerpo debe incluir los campos "id", "name", y "email"

  Scenario: Registrar usuario con email inválido
    When envío una solicitud POST a "/api/v1/users" con email "invalido"
    Then la respuesta debe tener código 400
    And debe mostrar un mensaje de error de validación

  Scenario: Registrar usuario ya existente
    Given existe un usuario registrado con el email "usuario@ejemplo.com"
    When intento registrar otro usuario con el mismo email
    Then la respuesta debe tener código 409
    And debe mostrar un mensaje "User already exists"


Feature: Listado de usuarios

  Scenario: Consultar lista de usuarios (camino feliz)
    Given existen usuarios registrados
    When envío una solicitud GET a "/api/v1/users?page=1&size=10"
    Then la respuesta debe tener código 200
    And debe retornar una lista de usuarios

  Scenario: Solicitar una página fuera de rango
    When envío GET a "/api/v1/users?page=9999&size=10"
    Then la respuesta debe tener código 404
    And debe incluir el mensaje "No users found"

  Scenario: Enviar parámetros inválidos
    When envío GET a "/api/v1/users?page=abc&size=-1"
    Then la respuesta debe tener código 400
    And debe incluir el mensaje "Invalid pagination parameters"


Feature: Consultar usuario por ID

  Scenario: Consultar usuario existente (camino feliz)
    Given existe un usuario con id 1
    When envío GET a "/api/v1/users/1"
    Then la respuesta debe tener código 200
    And el cuerpo debe incluir "email" y "name"

  Scenario: Consultar usuario inexistente
    When envío GET a "/api/v1/users/9999"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"

  Scenario: Consultar con ID inválido
    When envío GET a "/api/v1/users/abc"
    Then la respuesta debe tener código 400
    And debe mostrar "Invalid ID format"


Feature: Actualizar información de usuario

  Scenario: Actualizar usuario existente (camino feliz)
    Given existe un usuario con id 1
    When envío PUT a "/api/v1/users/1" con:
      | email                         | name                      |
      | usuario_actualizado@ejemplo.com | Andrés Rendón Actualizado |
    Then la respuesta debe tener código 200
    And los campos actualizados deben reflejarse en la respuesta

  Scenario: Intentar actualizar usuario inexistente
    When envío PUT a "/api/v1/users/9999"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"

  Scenario: Enviar datos inválidos
    When envío PUT con email vacío
    Then la respuesta debe tener código 400
    And debe mostrar "Invalid input data"


Feature: Eliminación de usuario

  Scenario: Eliminar usuario existente (camino feliz)
    Given existe un usuario con id 1
    When envío DELETE a "/api/v1/users/1"
    Then la respuesta debe tener código 200
    And debe mostrar "User deleted successfully"

  Scenario: Eliminar usuario inexistente
    When envío DELETE a "/api/v1/users/9999"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"

  Scenario: Enviar ID inválido
    When envío DELETE a "/api/v1/users/abc"
    Then la respuesta debe tener código 400
    And debe mostrar "Invalid ID format"


Feature: Actualizar estado de cuenta

  Scenario: Cambiar estado de cuenta correctamente (camino feliz)
    Given existe un usuario activo con id 1
    When envío PATCH a "/api/v1/users/1/account_status"
    Then la respuesta debe tener código 200
    And debe mostrar "Account status updated"

  Scenario: Cambiar estado de usuario inexistente
    When envío PATCH a "/api/v1/users/9999/account_status"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"

  Scenario: Enviar ID con formato inválido
    When envío PATCH a "/api/v1/users/abc/account_status"
    Then la respuesta debe tener código 400
    And debe mostrar "Invalid ID format"


Feature: Recuperar contraseña de usuario

  Scenario: Cambiar contraseña correctamente (camino feliz)
    Given el usuario "usuario@ejemplo.com" tiene un OTP válido
    When envío PATCH a "/api/v1/users/1/password" con:
      | email               | otp    | password       |
      | usuario@ejemplo.com | 493820 | Nuev0Pass2025  |
    Then la respuesta debe tener código 200
    And debe mostrar "Password updated successfully"

  Scenario: OTP incorrecto
    When envío PATCH con OTP "000000"
    Then la respuesta debe tener código 401
    And debe mostrar "Invalid OTP"

  Scenario: Usuario inexistente
    When envío PATCH a "/api/v1/users/9999/password"
    Then la respuesta debe tener código 404
    And debe mostrar "User not found"