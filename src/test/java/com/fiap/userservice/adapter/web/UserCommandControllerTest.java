package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.application.dto.request.CreateUserRequest;
import com.fiap.userservice.application.mapper.UserMapper;
import com.fiap.userservice.application.usecase.RegisterUserUseCase;
import com.fiap.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandController - Unit Tests")
class UserCommandControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private UserMapper mapper;

    private UserCommandController controller;

    @BeforeEach
    void setUp() {
        controller = new UserCommandController(registerUserUseCase, mapper);
    }

    @Test
    @DisplayName("createMorador: deve retornar 201 CREATED com DTO e Location quando sucesso")
    void testCreateMoradorSuccess() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        when(request.getUsername()).thenReturn("user1");
        when(request.getPassword()).thenReturn("pass");
        when(request.getPhone()).thenReturn("999999999");
        when(request.getApartment()).thenReturn("101");

        User createdUser = mock(User.class);

        UUID id = UUID.randomUUID();
        when(createdUser.getId()).thenReturn(id);

        UserDTO dto = new UserDTO();

        when(registerUserUseCase.execute(
                anyString(), anyString(), anyString(), anyString(), eq("MORADOR")
        )).thenReturn(createdUser);

        when(mapper.toDTO(createdUser)).thenReturn(dto);

        // Act
        ResponseEntity<?> response = controller.createMorador(request);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertSame(dto, response.getBody());
        assertNotNull(response.getHeaders().getLocation());
        assertEquals(URI.create("/api/internal/users/" + id), response.getHeaders().getLocation());

        verify(registerUserUseCase, times(1)).execute("user1", "pass", "999999999", "101", "MORADOR");
        verify(mapper, times(1)).toDTO(createdUser);
    }

    @Test
    @DisplayName("createPorteiro: deve retornar 201 CREATED com DTO e Location quando sucesso (apartment opcional)")
    void testCreatePorteiroSuccess() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        when(request.getUsername()).thenReturn("porteiro1");
        when(request.getPassword()).thenReturn("passp");
        when(request.getPhone()).thenReturn("888888888");
        when(request.getApartment()).thenReturn(null);

        User createdUser = mock(User.class);
        UUID id = UUID.randomUUID();
        when(createdUser.getId()).thenReturn(id);

        UserDTO dto = new UserDTO();

        when(registerUserUseCase.execute(
                anyString(), anyString(), anyString(), isNull(), eq("PORTEIRO")
        )).thenReturn(createdUser);

        when(mapper.toDTO(createdUser)).thenReturn(dto);

        // Act
        ResponseEntity<?> response = controller.createPorteiro(request);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertSame(dto, response.getBody());
        assertNotNull(response.getHeaders().getLocation());
        assertEquals(URI.create("/api/internal/users/" + id), response.getHeaders().getLocation());
        verify(registerUserUseCase, times(1)).execute("porteiro1", "passp", "888888888", null, "PORTEIRO");
        verify(mapper, times(1)).toDTO(createdUser);
    }

    @Test
    @DisplayName("createMorador: deve retornar 409 CONFLICT quando IllegalArgumentException indicar 'exists'")
    void testCreateMoradorConflictWhenExists() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        when(request.getUsername()).thenReturn("dup");
        when(request.getPassword()).thenReturn("p");
        when(request.getPhone()).thenReturn("1");
        when(request.getApartment()).thenReturn("1");

        when(registerUserUseCase.execute(anyString(), anyString(), anyString(), anyString(), eq("MORADOR")))
                .thenThrow(new IllegalArgumentException("User already exists"));

        // Act
        ResponseEntity<?> response = controller.createMorador(request);

        // Assert
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("User already exists", response.getBody());
        verify(registerUserUseCase, times(1)).execute("dup", "p", "1", "1", "MORADOR");
    }

    @Test
    @DisplayName("createPorteiro: deve retornar 400 BAD_REQUEST quando IllegalArgumentException genérica")
    void testCreatePorteiroBadRequestWhenInvalid() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        when(request.getUsername()).thenReturn("bad");
        when(request.getPassword()).thenReturn("p");
        when(request.getPhone()).thenReturn("1");
        when(request.getApartment()).thenReturn("1");

        when(registerUserUseCase.execute(anyString(), anyString(), anyString(), anyString(), eq("PORTEIRO")))
                .thenThrow(new IllegalArgumentException("invalid data"));

        // Act
        ResponseEntity<?> response = controller.createPorteiro(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("invalid data", response.getBody());
        verify(registerUserUseCase, times(1)).execute("bad", "p", "1", "1", "PORTEIRO");
    }

    @Test
    @DisplayName("createMorador: deve retornar 500 INTERNAL_SERVER_ERROR em caso de exceção não prevista")
    void testCreateMoradorInternalServerError() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        when(request.getUsername()).thenReturn("err");
        when(request.getPassword()).thenReturn("p");
        when(request.getPhone()).thenReturn("1");
        when(request.getApartment()).thenReturn("1");

        when(registerUserUseCase.execute(anyString(), anyString(), anyString(), anyString(), eq("MORADOR")))
                .thenThrow(new RuntimeException("boom"));

        // Act
        ResponseEntity<?> response = controller.createMorador(request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("internal error", response.getBody());
        verify(registerUserUseCase, times(1)).execute("err", "p", "1", "1", "MORADOR");
    }
}