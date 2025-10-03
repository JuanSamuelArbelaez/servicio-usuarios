package com.uniquindio.userservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // Usa el builder de Spring para mantener otras configuraciones por defecto
        ObjectMapper mapper = builder.build();
        // Registrar soporte para java.time (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());
        // Serializar fechas como ISO strings en vez de timestamps numéricos
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

