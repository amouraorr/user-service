package com.fiap.userservice.infrastructure.messaging;

import com.fiap.userservice.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Simples produtor Kafka que envia eventos de usuário.
 */
@Component
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${kafka.topic.users}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendUserCreatedEvent(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "USER_CREATED");
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        kafkaTemplate.send(topic, payload);
    }

    public void sendUserUpdatedEvent(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "USER_UPDATED");
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        kafkaTemplate.send(topic, payload);
    }
}