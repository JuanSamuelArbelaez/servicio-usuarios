package com.uniquindio.userservice.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record EventMessage(
        String id,                 // UUID único del evento
        EventType type,            // Enum con tipos de eventos
        String source,             // Qué microservicio lo emitió
        Instant timestamp,         // Cuándo se generó
        Object payload             // Datos específicos del evento
) {
    public static EventMessage of(EventType type, String source, Object payload) {
        return new EventMessage(
                UUID.randomUUID().toString(),
                type,
                source,
                Instant.now(),
                payload
        );
    }
}
