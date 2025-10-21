package com.fiap.userservice.domain.repository;

import com.fiap.userservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório do domínio (porta).
 */
public interface UserRepository {

    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    User update(User user);

}