package com.fiap.userservice.application.mapper;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.domain.model.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserDTO dto);
}