package com.uniquindio.userservice.filter;


import com.uniquindio.userservice.exception.authException.*;
import com.uniquindio.userservice.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/api/v1/auth/**",
            "/api/v1/users/*/password",
            "/api/v1/users/*/account_status",
            "/api/v1/users",            // controlaremos el método abajo
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/openapi.json"
    );


    @Override
    public void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Solo validar cuando corresponda
        if (!shouldNotFilter(request)) {
            // Aquí intentamos validar; si hay problemas, seteamos atributos y lanzamos la AuthenticationException correspondiente
            try {
                authenticateRequest(request);
                filterChain.doFilter(request, response);// tu método que valida el token y setea SecurityContext
            } catch (MissingTokenException ex) {
                // Request attributes para que el AuthenticationEntryPoint pueda leerlos
                request.setAttribute("JWT_ERROR_STATUS", HttpStatus.UNAUTHORIZED.value());
                request.setAttribute("JWT_ERROR_MESSAGE", ex.getMessage());
                throw new InsufficientAuthenticationException(ex.getMessage(), ex);
            } catch (ExpiredTokenException ex) {
                request.setAttribute("JWT_ERROR_STATUS", HttpStatus.FORBIDDEN.value());
                request.setAttribute("JWT_ERROR_MESSAGE", ex.getMessage());
                throw new CredentialsExpiredException(ex.getMessage(), ex);
            } catch (InvalidIssuerException ex) {
                request.setAttribute("JWT_ERROR_STATUS", HttpStatus.FORBIDDEN.value());
                request.setAttribute("JWT_ERROR_MESSAGE", ex.getMessage());
                throw new BadCredentialsException(ex.getMessage(), ex);
            } catch (RuntimeException ex) {
                // Error interno al validar el token
                request.setAttribute("JWT_ERROR_STATUS", HttpStatus.INTERNAL_SERVER_ERROR.value());
                request.setAttribute("JWT_ERROR_MESSAGE", "Error interno en la validación del token");
                // AuthenticationServiceException es apropiada para fallos del servicio de autenticación
                throw new AuthenticationServiceException("Error interno validando token", ex);
            }
        }
    }
    // ================== helpers ==================

    /**
     * Valida header/token, verifica issuer y subject, y si va bien, AUTENTICA en el contexto.
     */
    private void authenticateRequest(HttpServletRequest request) {
        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new MissingTokenException("Falta token JWT o formato inválido (se espera 'Bearer <token>')");
        }

        final String token = header.substring(7);

        try {
            Claims claims = jwtUtils.validateToken(token);

            if (!isIssuerValid(claims)) {
                throw new InvalidIssuerException("Emisor inválido");
            }

            // ✅ Si llegamos aquí: token válido → setear Authentication en el contexto
            setAuthentication(claims, token);

        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token expirado");
        } catch (SignatureException e) {
            throw new InvalidSignatureException("Firma del token inválida");
        } catch (JwtException e) {
            throw new MalformedTokenException("Token inválido o mal formado");
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al validar token", e);
        }
    }


    private boolean isIssuerValid(Claims claims) {
        return "ingesis.uniquindio.edu.co".equals(claims.getIssuer());
    }


    /** Construye el Authentication y lo coloca en el SecurityContext. */
    private void setAuthentication(Claims claims, String token) {
        String username = claims.getSubject();

        // Como no hay roles, pasamos una lista vacía — pero usamos el constructor con authorities
        Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, token, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("Usuario autenticado en contexto: {} — Authentication: {}",
                username, SecurityContextHolder.getContext().getAuthentication());
    }

    //Aca se deben agregar las rutas de los endpoints publicos
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }

        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) return true; // CORS

        for (String pattern : PUBLIC_PATTERNS) {
            if (PATH_MATCHER.match(pattern, path)) {
                // excepción: /api/v1/users solo si es POST
                if (pattern.equals("/api/v1/users")) {
                    return "POST".equalsIgnoreCase(method);
                }
                return true;
            }
        }
        return false;
    }


}


