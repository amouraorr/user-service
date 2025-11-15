package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.LoginResponse;
import com.fiap.userservice.application.dto.request.LoginRequest;
import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.application.usecase.GetUserUseCase;
import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.infrastructure.config.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private AppPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(getUserUseCase, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("POST /login - Deve retornar token quando credenciais estiverem válidas")
    void login_ReturnsToken_WhenCredentialsValid() {
        // Arrange
        LoginRequest request = mock(LoginRequest.class);
        when(request.getUsername()).thenReturn("johndoe");
        when(request.getPassword()).thenReturn("rawPass");

        User user = mock(User.class);
        when(getUserUseCase.findByUsername("johndoe")).thenReturn(user);
        when(user.getPassword()).thenReturn("encodedPass");
        when(user.getUsername()).thenReturn("johndoe");
        when(user.getRole()).thenReturn("ROLE_USER");

        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtTokenProvider.createToken("johndoe", "ROLE_USER")).thenReturn("token123");

        // Act
        ResponseEntity<?> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals("token123", body.getToken());

        // Verify
        verify(getUserUseCase).findByUsername("johndoe");
        verify(passwordEncoder).matches("rawPass", "encodedPass");
        verify(jwtTokenProvider).createToken("johndoe", "ROLE_USER");
        verifyNoMoreInteractions(getUserUseCase, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("POST /login - Deve retornar 401 quando a senha for inválida")
    void login_Returns401_WhenPasswordInvalid() {
        // Arrange
        LoginRequest request = mock(LoginRequest.class);
        when(request.getUsername()).thenReturn("johndoe");
        when(request.getPassword()).thenReturn("wrongPass");

        User user = mock(User.class);
        when(getUserUseCase.findByUsername("johndoe")).thenReturn(user);
        when(user.getPassword()).thenReturn("encodedPass");

        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("invalid credentials", response.getBody());

        // Verify
        verify(getUserUseCase).findByUsername("johndoe");
        verify(passwordEncoder).matches("wrongPass", "encodedPass");
        verifyNoMoreInteractions(getUserUseCase, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("POST /login - Deve retornar 401 quando usuário não for encontrado (IllegalArgumentException)")
    void login_Returns401_WhenFindByUsernameThrowsIllegalArgumentException() {
        // Arrange
        LoginRequest request = mock(LoginRequest.class);
        when(request.getUsername()).thenReturn("unknown");
        when(getUserUseCase.findByUsername("unknown")).thenThrow(new IllegalArgumentException("not found"));

        // Act
        ResponseEntity<?> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("invalid credentials", response.getBody());

        // Verify
        verify(getUserUseCase).findByUsername("unknown");
        verifyNoMoreInteractions(getUserUseCase, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("POST /login - Deve retornar 500 quando ocorrer erro interno")
    void login_Returns500_WhenInternalExceptionOccurs() {
        // Arrange
        LoginRequest request = mock(LoginRequest.class);
        when(request.getUsername()).thenReturn("johndoe");
        when(request.getPassword()).thenReturn("rawPass");

        User user = mock(User.class);
        when(getUserUseCase.findByUsername("johndoe")).thenReturn(user);
        when(user.getPassword()).thenReturn("encodedPass");
        when(user.getUsername()).thenReturn("johndoe");
        when(user.getRole()).thenReturn("ROLE_USER");

        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtTokenProvider.createToken("johndoe", "ROLE_USER")).thenThrow(new RuntimeException("boom"));

        // Act
        ResponseEntity<?> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("internal error", response.getBody());

        // Verify
        verify(getUserUseCase).findByUsername("johndoe");
        verify(passwordEncoder).matches("rawPass", "encodedPass");
        verify(jwtTokenProvider).createToken("johndoe", "ROLE_USER");
        verifyNoMoreInteractions(getUserUseCase, passwordEncoder, jwtTokenProvider);
    }
}