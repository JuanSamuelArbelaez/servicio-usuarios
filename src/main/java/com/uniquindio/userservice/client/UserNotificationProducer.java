package com.uniquindio.userservice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.userservice.dto.OtpResponse;
import com.uniquindio.userservice.dto.UserAuthResponse;
import com.uniquindio.userservice.dto.UserResponse;
import com.uniquindio.userservice.dto.notification.EventMessage;
import com.uniquindio.userservice.dto.notification.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserNotificationProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Jackson para serializar eventos

    public void sendUserLogin(UserAuthResponse user) {
        EventMessage event = EventMessage.of(
                EventType.USER_LOGIN,
                "auth-service",
                Map.of(
                        "id", user.id(),
                        "name", user.name(),
                        "email", user.email(),
                        "phone", user.phone()
                )
        );
        send(event);
    }

    public void sendRequestOtp(UserAuthResponse user, OtpResponse otp) {
        EventMessage event = EventMessage.of(
                EventType.OTP_REQUESTED,
                "auth-service",
                Map.of(
                        "id", user.id(),
                        "name", user.name(),
                        "email", user.email(),
                        "phone", user.phone(),
                        "url-recovery", otp.url()
                )
        );
        send(event);
    }


    //Envia un evento a kafka sobre la creacipon de un nuevo usuario en el sistema
    public void sendWelcome(UserResponse user) {
        EventMessage event = EventMessage.of(
                EventType.USER_REGISTERED,
                "user-service",
                Map.of(
                        "id", user.id(),
                        "name", user.name(),
                        "email", user.email(),
                        "phone", user.phone(),
                        "url", "http://local-host:8080/api/v1/users/"+user.id()+"/account_status"
                )
        );
        send(event);
    }

    //Envia un evento a kafka sobre la creacipon de un nuevo usuario en el sistema
    public void sendPasswordChanged(UserResponse user) {
        EventMessage event = EventMessage.of(
                EventType.PASSWORD_CHANGED,
                "user-service",
                Map.of(
                        "id", user.id(),
                        "name", user.name(),
                        "email", user.email(),
                        "phone", user.phone()
                )
        );
        send(event);
    }


    private void send(EventMessage event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", event.id(), eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando evento", e);//Agregar excepcion personalizada
        }
    }

    public void sendAccountVerified(UserResponse user) {
        EventMessage event = EventMessage.of(
                EventType.USER_VERIFIED,
                "user-service",
                Map.of(
                        "id", user.id(),
                        "name", user.name(),
                        "email", user.email()
                )
        );
        send(event);
    }
}

