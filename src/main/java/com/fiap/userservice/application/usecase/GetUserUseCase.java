package com.fiap.userservice.application.usecase;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso para obter usuários (leitura).
 */
@Service
public class GetUserUseCase {

    private final UserRepository repository;

    public GetUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna um usuário por id ou lança IllegalArgumentException se não encontrado.
     */
    public User findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    /**
     * Retorna um usuário por username ou lança IllegalArgumentException se não encontrado.
     */
    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }
}