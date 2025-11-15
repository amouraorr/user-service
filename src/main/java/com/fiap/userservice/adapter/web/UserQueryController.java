package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.application.usecase.GetUserUseCase;
import com.fiap.userservice.application.mapper.UserMapper;
import com.fiap.userservice.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints para consulta de usuários por outros serviços (ex.: Parcel Service).
 * Rotas em /api/internal/users para deixar claro o uso interno/síncrono.
 */
@Tag(name = "User", description = "Endpoints para consulta de usuários")
@RestController
@RequestMapping("/api/internal/users")
public class UserQueryController {

    private final GetUserUseCase getUserUseCase;
    private final UserMapper mapper;

    public UserQueryController(GetUserUseCase getUserUseCase, UserMapper mapper) {
        this.getUserUseCase = getUserUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar moradores", description = "Retorna todos os usuários com role = MORADOR")
    @GetMapping("/moradores")
    public ResponseEntity<List<UserDTO>> getMoradores() {
        List<User> users = getUserUseCase.findAllByRole("MORADOR");
        List<UserDTO> dtos = users.stream().map(mapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar porteiros", description = "Retorna todos os usuários com role = PORTEIRO")
    @GetMapping("/porteiros")
    public ResponseEntity<List<UserDTO>> getPorteiros() {
        List<User> users = getUserUseCase.findAllByRole("PORTEIRO");
        List<UserDTO> dtos = users.stream().map(mapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}