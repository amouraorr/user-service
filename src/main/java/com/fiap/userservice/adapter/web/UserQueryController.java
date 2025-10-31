package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.application.usecase.GetUserUseCase;
import com.fiap.userservice.application.mapper.UserMapper;
import com.fiap.userservice.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints para consulta de usuários por outros serviços (ex.: Parcel Service).
 * Rotas em /api/internal/users para deixar claro o uso interno/síncrono.
 */
@RestController
@RequestMapping("/api/internal/users")
public class UserQueryController {

    private final GetUserUseCase getUserUseCase;
    private final UserMapper mapper;

    public UserQueryController(GetUserUseCase getUserUseCase, UserMapper mapper) {
        this.getUserUseCase = getUserUseCase;
        this.mapper = mapper;
    }

    /**
     * GET /api/internal/users/{id}
     * Retorna UserDTO para integração síncrona com outros microsserviços (Parcel Service).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable UUID id) {
        User user = getUserUseCase.findById(id);
        UserDTO dto = mapper.toDTO(user);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/internal/users/by-username/{username}
     * Retorna UserDTO para integração síncrona com outros microsserviços (Parcel Service).
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getByUsername(@PathVariable String username) {
        User user = getUserUseCase.findByUsername(username);
        UserDTO dto = mapper.toDTO(user);
        return ResponseEntity.ok(dto);
    }
}