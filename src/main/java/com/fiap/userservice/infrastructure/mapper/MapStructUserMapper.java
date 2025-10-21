package com.fiap.userservice.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.fiap.userservice.application.mapper.UserMapper;

@Mapper(componentModel = "spring")
public interface MapStructUserMapper extends UserMapper {

}