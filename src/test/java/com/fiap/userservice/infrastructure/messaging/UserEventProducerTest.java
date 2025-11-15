package com.fiap.userservice.infrastructure.messaging;

import com.fiap.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para UserEventProducer")
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private User user;

    @Captor
    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    private UserEventProducer producer;
    private static final String TOPIC = "users-topic";

    @BeforeEach
    void setUp() {
        producer = new UserEventProducer(kafkaTemplate, TOPIC);
    }

    @Test
    @DisplayName("Enviar evento USER_CREATED com payload correto")
    void testSendUserCreatedEvent() {
        // Arrange
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn("john_doe");

        // Act
        producer.sendUserCreatedEvent(user);

        // Verify
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), payloadCaptor.capture());

        // Assert
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("USER_CREATED", payload.get("event"));
        assertEquals(id, payload.get("id"));
        assertEquals("john_doe", payload.get("username"));
    }

    @Test
    @DisplayName("Enviar evento USER_UPDATED com payload correto")
    void testSendUserUpdatedEvent() {
        // Arrange
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn("jane_doe");

        // Act
        producer.sendUserUpdatedEvent(user);

        // Verify
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), payloadCaptor.capture());

        // Assert
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("USER_UPDATED", payload.get("event"));
        assertEquals(id, payload.get("id"));
        assertEquals("jane_doe", payload.get("username"));
    }
}