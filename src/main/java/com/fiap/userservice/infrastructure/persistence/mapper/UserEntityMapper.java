package com.fiap.userservice.infrastructure.persistence.mapper;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.UUID;

/**
 * Mapper MapStruct conversão entre User (domínio) e UserEntity (persistência).
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(User user);

    User toDomain(UserEntity entity);

    default UUID map(Long value) {
        if (value == null) {
            return null;
        }
        return new UUID(0L, value);
    }

    default Long map(UUID value) {
        if (value == null) {
            return null;
        }
        return value.getLeastSignificantBits();
    }
}