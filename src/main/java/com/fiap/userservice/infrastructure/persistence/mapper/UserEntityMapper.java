package com.fiap.userservice.infrastructure.persistence.mapper;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct responsável pela conversão entre User (domínio) e UserEntity (persistência).
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(User user);

    User toDomain(UserEntity entity);
}