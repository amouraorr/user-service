package com.fiap.userservice.adapter.web;

import com.fiap.userservice.application.dto.UserDTO;
import com.fiap.userservice.application.mapper.UserMapper;
import com.fiap.userservice.application.usecase.GetUserUseCase;
import com.fiap.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserQueryControllerTest {

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UserMapper mapper;

    private UserQueryController controller;

    @BeforeEach
    void setUp() {
        controller = new UserQueryController(getUserUseCase, mapper);
    }

    @Test
    @DisplayName("GET /moradores - Deve retornar lista vazia quando não houver moradores")
    void getMoradores_ReturnsEmptyList_WhenNoUsers() {
        // Arrange
        when(getUserUseCase.findAllByRole("MORADOR")).thenReturn(List.of());

        // Act
        ResponseEntity<List<UserDTO>> response = controller.getMoradores();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // Verify
        verify(getUserUseCase).findAllByRole("MORADOR");
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("GET /moradores - Deve retornar DTOs mapeados quando houver moradores")
    void getMoradores_ReturnsMappedDTOs_WhenUsersExist() {
        // Arrange
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        UserDTO dto1 = new UserDTO();
        dto1.setId(UUID.fromString("00000000-0000-0000-0000-000000000001")); // passo 2
        dto1.setUsername("user1");
        UserDTO dto2 = new UserDTO();
        dto2.setId(UUID.fromString("00000000-0000-0000-0000-000000000002")); // passo 2
        dto2.setUsername("user2");

        when(getUserUseCase.findAllByRole("MORADOR")).thenReturn(List.of(user1, user2));
        when(mapper.toDTO(user1)).thenReturn(dto1);
        when(mapper.toDTO(user2)).thenReturn(dto2);

        // Act
        ResponseEntity<List<UserDTO>> response = controller.getMoradores();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<UserDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals(dto1, body.get(0));
        assertEquals(dto2, body.get(1));

        // Verify
        verify(getUserUseCase).findAllByRole("MORADOR");
        verify(mapper).toDTO(user1);
        verify(mapper).toDTO(user2);
        verifyNoMoreInteractions(getUserUseCase, mapper);
    }

    @Test
    @DisplayName("GET /porteiros - Deve retornar lista vazia quando não houver porteiros")
    void getPorteiros_ReturnsEmptyList_WhenNoUsers() {
        // Arrange
        when(getUserUseCase.findAllByRole("PORTEIRO")).thenReturn(List.of());

        // Act
        ResponseEntity<List<UserDTO>> response = controller.getPorteiros();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // Verify
        verify(getUserUseCase).findAllByRole("PORTEIRO");
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("GET /porteiros - Deve retornar DTOs mapeados quando houver porteiros")
    void getPorteiros_ReturnsMappedDTOs_WhenUsersExist() {
        // Arrange
        User user = mock(User.class);
        UserDTO dto = new UserDTO();
        dto.setId(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        dto.setUsername("porteiro1");

        when(getUserUseCase.findAllByRole("PORTEIRO")).thenReturn(List.of(user));
        when(mapper.toDTO(user)).thenReturn(dto);

        // Act
        ResponseEntity<List<UserDTO>> response = controller.getPorteiros();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<UserDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(dto, body.get(0));

        // Verify
        verify(getUserUseCase).findAllByRole("PORTEIRO");
        verify(mapper).toDTO(user);
        verifyNoMoreInteractions(getUserUseCase, mapper);
    }
}