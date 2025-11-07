package com.fiap.userservice.application.usecase;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetUserUseCase {

    private final UserRepository repository;

    public GetUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public User findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public List<User> findAllByRole(String role) {
        return repository.findAllByRole(role);
    }
}