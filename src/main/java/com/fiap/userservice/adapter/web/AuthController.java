package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.LoginResponse;
import com.fiap.userservice.application.dto.request.LoginRequest;
import com.fiap.userservice.application.usecase.GetUserUseCase;
import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.infrastructure.config.security.JwtTokenProvider;
import com.fiap.userservice.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Endpoints de autenticação (emissão de token JWT).
 */
@Tag(name = "Auth", description = "Endpoints de autenticação (emissão de token JWT)")
@RestController
@RequestMapping("/api/internal/auth")
@Validated
public class AuthController {

    private final GetUserUseCase getUserUseCase;
    private final AppPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(GetUserUseCase getUserUseCase,
                          AppPasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.getUserUseCase = getUserUseCase;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * POST /api/internal/auth/login
     * Retorna JWT se credenciais estiverem corretas.
     */
    @Operation(summary = "Emitir token JWT", description = "Autentica o usuário e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = getUserUseCase.findByUsername(request.getUsername());
            boolean ok = passwordEncoder.matches(request.getPassword(), user.getPassword());
            if (!ok) {
                return ResponseEntity.status(401).body("invalid credentials");
            }
            String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body("invalid credentials");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("internal error");
        }
    }
}