package com.fiap.userservice.infrastructure.persistence.adapter;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.persistence.entity.UserEntity;
import com.fiap.userservice.infrastructure.persistence.mapper.UserEntityMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do adaptador de persistência (porta -> adaptador).
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserEntityRepository jpaRepo;
    private final UserEntityMapper mapper;

    public UserRepositoryImpl(JpaUserEntityRepository jpaRepo, UserEntityMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepo.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public User update(User user) {
        UserEntity entity = mapper.toEntity(user);
        entity.setUpdatedAt(OffsetDateTime.now());
        UserEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }
}