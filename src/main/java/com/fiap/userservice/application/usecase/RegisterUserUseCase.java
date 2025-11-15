package com.fiap.userservice.application.usecase;

import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.messaging.UserEventProducer;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para registrar usuário (porta de aplicação)
 */
@Service
public class RegisterUserUseCase {

    private final UserRepository repository;
    private final AppPasswordEncoder appPasswordEncoder;
    private final Optional<UserEventProducer> eventProducer;

    public RegisterUserUseCase(UserRepository repository,
                               AppPasswordEncoder appPasswordEncoder,
                               Optional<UserEventProducer> eventProducer) {
        this.repository = repository;
        this.appPasswordEncoder = appPasswordEncoder;
        this.eventProducer = eventProducer;
    }

    public User execute(String username, String rawPassword, String phone, String apartment, String role) {
        Optional<User> existing = repository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        String hashed = appPasswordEncoder.encode(rawPassword);

        User user = new User(
                UUID.randomUUID(),
                username,
                hashed,
                phone,
                apartment,
                role,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        User saved = repository.save(user);
        // envia evento apenas se o produtor estiver disponível
        eventProducer.ifPresent(ep -> ep.sendUserCreatedEvent(saved));
        return saved;
    }
}