package com.fiap.userservice.application.usecase;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.messaging.UserEventProducer;
import com.fiap.userservice.application.security.AppPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Caso de uso para atualizar dados do usuário (requer autenticação).
 */
@Service
public class UpdateUserUseCase {

    private final UserRepository repository;
    private final AppPasswordEncoder appPasswordEncoder;
    private final UserEventProducer eventProducer;

    public UpdateUserUseCase(UserRepository repository, AppPasswordEncoder appPasswordEncoder, UserEventProducer eventProducer) {
        this.repository = repository;
        this.appPasswordEncoder = appPasswordEncoder;
        this.eventProducer = eventProducer;
    }

    public User execute(UUID id, String phone, String apartment, String rawPassword) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
        if (phone != null) user.setPhone(phone);
        if (apartment != null) user.setApartment(apartment);
        if (rawPassword != null) user.setPassword(appPasswordEncoder.encode(rawPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        User updated = repository.update(user);
        eventProducer.sendUserUpdatedEvent(updated);
        return updated;
    }
}