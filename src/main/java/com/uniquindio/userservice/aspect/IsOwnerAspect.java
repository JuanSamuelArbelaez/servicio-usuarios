package com.uniquindio.userservice.aspect;


import com.uniquindio.userservice.exception.authException.UnauthorizedOwnerAccessException;
import com.uniquindio.userservice.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IsOwnerAspect {

    private final JwtUtils jwtUtils; // tu helper que valida/parsea el token

    @Before("@annotation(com.uniquindio.userservice.annotation.IsOwner) && args(id,..)")
    public void checkOwnership(JoinPoint jp, Object id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Recuperamos el token
        String token = (String) auth.getCredentials();
        log.info("token en la validación: {}", token);

        Claims claims = jwtUtils.getClaims(token);
        int userIdFromToken = claims.get("userId", Integer.class);
        log.info("user id: {}", userIdFromToken);

        log.debug("Validando acceso con claim userId: userIdToken={}, idParametro={}", userIdFromToken, id);

        int userId = Integer.parseInt(id.toString());

        if (userIdFromToken != userId) {
            throw new UnauthorizedOwnerAccessException(
                    "Acceso denegado: no tienes permisos para modificar este recurso"
            );
        }

    }

}
