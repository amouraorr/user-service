package com.fiap.userservice.infrastructure.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;

import com.fiap.userservice.application.mapper.UserMapper;

@Mapper(componentModel = "spring")
public interface MapStructUserMapper extends UserMapper {

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