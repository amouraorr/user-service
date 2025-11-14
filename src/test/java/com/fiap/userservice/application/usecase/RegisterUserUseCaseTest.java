package com.fiap.userservice.application.usecase;

import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.messaging.UserEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AppPasswordEncoder appPasswordEncoder;

    @Mock
    private UserEventProducer eventProducer;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @InjectMocks
    private RegisterUserUseCase ignoredInjectMock;

    @Test
    @DisplayName("execute - deve salvar usuário e enviar evento quando produtor presente")
    void execute_shouldSaveUserAndSendEvent_whenProducerPresent() {
        // Arrange
        when(repository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(appPasswordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository, appPasswordEncoder, Optional.of(eventProducer));
        User result = useCase.execute("johndoe", "plainPassword", "123456789", "101", "USER");

        // Assert
        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getId(), "id should be generated");
        assertEquals("johndoe", savedUser.getUsername());
        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals("123456789", savedUser.getPhone());
        assertEquals("101", savedUser.getApartment());
        assertEquals("USER", savedUser.getRole());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        assertEquals(savedUser, result);

        // Verify
        verify(repository).findByUsername("johndoe");
        verify(eventProducer).sendUserCreatedEvent(savedUser);
        verifyNoMoreInteractions(repository, appPasswordEncoder, eventProducer);
    }

    @Test
    @DisplayName("execute - deve salvar usuário e não enviar evento quando produtor ausente")
    void execute_shouldSaveUserAndNotSendEvent_whenProducerAbsent() {
        // Arrange
        when(repository.findByUsername("janedoe")).thenReturn(Optional.empty());
        when(appPasswordEncoder.encode("pw")).thenReturn("hashedPw");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository, appPasswordEncoder, Optional.empty());
        User result = useCase.execute("janedoe", "pw", "987654321", "202", "ADMIN");

        // Assert
        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getId());
        assertEquals("janedoe", savedUser.getUsername());
        assertEquals("hashedPw", savedUser.getPassword());
        assertEquals("987654321", savedUser.getPhone());
        assertEquals("202", savedUser.getApartment());
        assertEquals("ADMIN", savedUser.getRole());
        assertEquals(savedUser, result);

        // Verify
        verify(repository).findByUsername("janedoe");
        verifyNoInteractions(eventProducer);
        verifyNoMoreInteractions(repository, appPasswordEncoder);
    }

    @Test
    @DisplayName("execute - deve lançar IllegalArgumentException quando o username já existir")
    void execute_shouldThrow_whenUsernameAlreadyExists() {
        // Arrange
        User existing = new User(UUID.randomUUID(), "existing", "x", "phone", "apt", "ROLE", OffsetDateTime.now(), OffsetDateTime.now());
        when(repository.findByUsername("existing")).thenReturn(Optional.of(existing));

        // Act & Assert
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository, appPasswordEncoder, Optional.of(eventProducer));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("existing", "any", "phone", "apt", "ROLE"));

        assertEquals("username already exists", ex.getMessage());

        // Verify
        verify(repository).findByUsername("existing");
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(appPasswordEncoder, eventProducer);
    }
}