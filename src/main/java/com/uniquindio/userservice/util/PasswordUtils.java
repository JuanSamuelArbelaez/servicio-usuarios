package com.uniquindio.userservice.util;

import com.uniquindio.userservice.dto.PasswordRecoveryRequest;
import com.uniquindio.userservice.dto.UserRegistration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtils {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Devuelve un nuevo UserRegistration con la contraseña encriptada.
     */
    public static UserRegistration encryptPassword(UserRegistration user) {
        String encryptedPassword = passwordEncoder.encode(user.password());
        return new UserRegistration(user.email(), encryptedPassword, user.name(), user.phone());
    }


    public static PasswordRecoveryRequest encryptPassword(PasswordRecoveryRequest passwordRecoveryRequest) {
        String encryptedPassword = passwordEncoder.encode(passwordRecoveryRequest.password());
        return new PasswordRecoveryRequest(passwordRecoveryRequest.email(), passwordRecoveryRequest.otp(), encryptedPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}

