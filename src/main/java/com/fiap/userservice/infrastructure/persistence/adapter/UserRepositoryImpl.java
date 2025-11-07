package com.fiap.userservice.infrastructure.persistence.adapter;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.persistence.entity.UserEntity;
import com.fiap.userservice.infrastructure.persistence.mapper.UserEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
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
        UserEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<User> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllByRole(String role) {
        return jpaRepo.findAllByRole(role).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
