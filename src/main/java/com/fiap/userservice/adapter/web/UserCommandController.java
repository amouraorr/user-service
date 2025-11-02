package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.application.dto.request.CreateUserRequest;
import com.fiap.userservice.application.mapper.UserMapper;
import com.fiap.userservice.application.usecase.RegisterUserUseCase;
import com.fiap.userservice.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Endpoints para criação/atualização de usuários.
 */
@RestController
@RequestMapping("/api/internal/users")
public class UserCommandController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserMapper mapper;

    public UserCommandController(RegisterUserUseCase registerUserUseCase, UserMapper mapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.mapper = mapper;
    }

    /**
     * Cria um Morador (role = "MORADOR").
     */
    @PostMapping("/morador")
    public ResponseEntity<?> createMorador(@Valid @RequestBody CreateUserRequest request) {
        try {
            User created = registerUserUseCase.execute(
                    request.getUsername(),
                    request.getPassword(),
                    request.getPhone(),
                    request.getApartment(),
                    "MORADOR"
            );
            UserDTO dto = mapper.toDTO(created);
            URI location = URI.create("/api/internal/users/" + created.getId());
            return ResponseEntity.created(location).body(dto);
        } catch (IllegalArgumentException ex) {

            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        }
    }

    /**
     * Cria um Porteiro (role = "PORTEIRO").
     * apartment pode ser nulo/omisso.
     */
    @PostMapping("/porteiro")
    public ResponseEntity<?> createPorteiro(@Valid @RequestBody CreateUserRequest request) {
        try {
            User created = registerUserUseCase.execute(
                    request.getUsername(),
                    request.getPassword(),
                    request.getPhone(),
                    request.getApartment(),
                    "PORTEIRO"
            );
            UserDTO dto = mapper.toDTO(created);
            URI location = URI.create("/api/internal/users/" + created.getId());
            return ResponseEntity.created(location).body(dto);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        }
    }
}