package com.uniquindio.userservice.exceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.userservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {


    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Integer status = (Integer) request.getAttribute("JWT_ERROR_STATUS");
        String message = (String) request.getAttribute("JWT_ERROR_MESSAGE");

        if (status == null) status = HttpStatus.UNAUTHORIZED.value();
        if (message == null) message = authException != null ? authException.getMessage() : "Unauthorized";

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        ErrorResponse error = new ErrorResponse(status, message, LocalDateTime.now());

        PrintWriter writer = response.getWriter();
        try {
            mapper.writeValue(writer, error);
            writer.flush();
        } catch (Exception ex) {
            log.warn("Error serializando ErrorResponse en AuthenticationEntryPoint: {}", ex.getMessage());
            try {
                String fallback = String.format("{\"status\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                        status,
                        escapeJson(message),
                        LocalDateTime.now().toString());
                writer.write(fallback);
                writer.flush();
            } catch (Exception writeEx) {
                log.error("No se pudo escribir la respuesta de error en el AuthenticationEntryPoint: {}", writeEx.getMessage());
            }
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
