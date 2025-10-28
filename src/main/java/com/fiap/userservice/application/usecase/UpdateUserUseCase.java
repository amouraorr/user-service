package com.fiap.userservice.application.usecase;

import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.messaging.UserEventProducer;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateUserUseCase {

    private final UserRepository repository;
    private final AppPasswordEncoder appPasswordEncoder;
    private final Optional<UserEventProducer> eventProducer;

    public UpdateUserUseCase(UserRepository repository,
                             AppPasswordEncoder appPasswordEncoder,
                             Optional<UserEventProducer> eventProducer) {
        this.repository = repository;
        this.appPasswordEncoder = appPasswordEncoder;
        this.eventProducer = eventProducer;
    }

    /**
     * Atualiza um usuário encontrado pelo id. Campos nulos são ignorados (não atualizados).
     */
    public User execute(UUID id,
                        String username,
                        String rawPassword,
                        String phone,
                        String apartment,
                        String role) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }

        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            String hashed = appPasswordEncoder.encode(rawPassword);
            user.setPassword(hashed);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        if (apartment != null) {
            user.setApartment(apartment);
        }

        if (role != null) {
            user.setRole(role);
        }

        user.setUpdatedAt(OffsetDateTime.now());

        User saved = repository.save(user);

        // envia evento apenas se o produtor estiver disponível
        eventProducer.ifPresent(ep -> ep.sendUserUpdatedEvent(saved));

        return saved;
    }
}