package com.fiap.userservice.application.usecase;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUserUseCaseTest {

    @Mock
    private UserRepository repository;

    private GetUserUseCase useCase;

    @BeforeEach
    void setUp() {

        useCase = new GetUserUseCase(repository);
    }

    @Test
    @DisplayName("findById - deve retornar usuário quando presente")
    void findById_ShouldReturnUser_WhenPresent() {

        UUID id = UUID.randomUUID();
        User user = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        User result = useCase.findById(id);

        assertNotNull(result);
        assertEquals(user, result);
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("findById - deve lançar IllegalArgumentException quando usuário não encontrado")
    void findById_ShouldThrow_WhenNotFound() {

        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.findById(id));
        assertEquals("usuário não encontrado", ex.getMessage());
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("findByUsername - deve retornar usuário quando presente")
    void findByUsername_ShouldReturnUser_WhenPresent() {

        String username = "johndoe";
        User user = mock(User.class);
        when(repository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = useCase.findByUsername(username);

        assertNotNull(result);
        assertEquals(user, result);
        verify(repository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("findByUsername - deve lançar IllegalArgumentException quando usuário não encontrado")
    void findByUsername_ShouldThrow_WhenNotFound() {

        String username = "nouser";
        when(repository.findByUsername(username)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.findByUsername(username));
        assertEquals("usuário não encontrado", ex.getMessage());
        verify(repository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("findAll - deve retornar lista de usuários do repositório")
    void findAll_ShouldReturnList() {

        User u1 = mock(User.class);
        User u2 = mock(User.class);
        List<User> users = Arrays.asList(u1, u2);
        when(repository.findAll()).thenReturn(users);

        List<User> result = useCase.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(users, result);
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - deve retornar lista vazia quando não houver usuários")
    void findAll_ShouldReturnEmptyList_WhenNone() {

        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = useCase.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllByRole - deve retornar usuários conforme papel")
    void findAllByRole_ShouldReturnList() {

        String role = "ADMIN";
        User u1 = mock(User.class);
        List<User> users = Collections.singletonList(u1);
        when(repository.findAllByRole(role)).thenReturn(users);

        List<User> result = useCase.findAllByRole(role);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(users, result);
        verify(repository, times(1)).findAllByRole(role);
    }
}