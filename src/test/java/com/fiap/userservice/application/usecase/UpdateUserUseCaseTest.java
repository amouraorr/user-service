package com.fiap.userservice.application.usecase;

import com.fiap.userservice.application.security.AppPasswordEncoder;
import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.domain.repository.UserRepository;
import com.fiap.userservice.infrastructure.messaging.UserEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AppPasswordEncoder appPasswordEncoder;

    @Mock
    private UserEventProducer eventProducer;

    @Captor
    private ArgumentCaptor<OffsetDateTime> updatedAtCaptor;

    private UpdateUserUseCase useCaseWithProducer;
    private UpdateUserUseCase useCaseWithoutProducer;

    @BeforeEach
    void setUp() {
        useCaseWithProducer = new UpdateUserUseCase(repository, appPasswordEncoder, Optional.of(eventProducer));
        useCaseWithoutProducer = new UpdateUserUseCase(repository, appPasswordEncoder, Optional.empty());
    }

    @Test
    @DisplayName("Deve atualizar todos os campos válidos e enviar evento quando produtor disponível")
    void deveAtualizarCamposEVazarEventoQuandoProdutorDisponivel() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(appPasswordEncoder.encode("novaSenha")).thenReturn("hashedSenha");
        when(repository.save(existingUser)).thenReturn(existingUser);

        // Act
        User result = useCaseWithProducer.execute(id, "novoUser", "novaSenha", "11999999999", "101", "ADMIN");

        // Assert
        verify(existingUser).setUsername("novoUser");
        verify(existingUser).setPassword("hashedSenha");
        verify(existingUser).setPhone("11999999999");
        verify(existingUser).setApartment("101");
        verify(existingUser).setRole("ADMIN");
        verify(existingUser).setUpdatedAt(updatedAtCaptor.capture());
        OffsetDateTime captured = updatedAtCaptor.getValue();
        assertNotNull(captured);

        // Verify
        verify(repository).save(existingUser);
        verify(eventProducer).sendUserUpdatedEvent(existingUser);

        assertSame(existingUser, result);
    }

    @Test
    @DisplayName("Não deve atualizar username quando for null")
    void naoDeveAtualizarUsernameQuandoNull() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(repository.save(existingUser)).thenReturn(existingUser);

        // Act
        useCaseWithProducer.execute(id, null, "novaSenha", null, null, null);

        // Assert
        verify(existingUser, never()).setUsername(anyString());

        // Verify
        verify(repository).save(existingUser);
        verify(eventProducer).sendUserUpdatedEvent(existingUser);
    }

    @Test
    @DisplayName("Não deve atualizar username quando for vazio ou apenas espaços")
    void naoDeveAtualizarUsernameQuandoVazio() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(repository.save(existingUser)).thenReturn(existingUser);

        // Act
        useCaseWithProducer.execute(id, "   ", "novaSenha", null, null, null);

        // Assert
        verify(existingUser, never()).setUsername(anyString());

        // Verify
        verify(repository).save(existingUser);
        verify(eventProducer).sendUserUpdatedEvent(existingUser);
    }

    @Test
    @DisplayName("Não deve atualizar senha quando for null ou vazia")
    void naoDeveAtualizarSenhaQuandoNullOuVazio() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(repository.save(existingUser)).thenReturn(existingUser);

        // Act - senha null
        useCaseWithProducer.execute(id, null, null, null, null, null);
        // Act - senha vazia
        useCaseWithProducer.execute(id, null, "   ", null, null, null);

        // Assert
        verify(existingUser, never()).setPassword(anyString());
        verify(appPasswordEncoder, never()).encode(anyString());

        // Verify
        verify(repository, times(2)).save(existingUser);
        verify(eventProducer, times(2)).sendUserUpdatedEvent(existingUser);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando usuário não for encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                useCaseWithProducer.execute(id, "u", "p", null, null, null)
        );
        assertEquals("usuário não encontrado", thrown.getMessage());

        // Verify
        verify(repository, never()).save(any());
        verify(eventProducer, never()).sendUserUpdatedEvent(any());
    }

    @Test
    @DisplayName("Não deve enviar evento quando o produtor não estiver disponível")
    void naoDeveEnviarEventoQuandoProdutorIndisponivel() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = mock(User.class);
        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(appPasswordEncoder.encode("senha")).thenReturn("hashed");
        when(repository.save(existingUser)).thenReturn(existingUser);

        // Act
        useCaseWithoutProducer.execute(id, "user", "senha", null, null, null);

        // Assert
        verify(existingUser).setUsername("user");
        verify(existingUser).setPassword("hashed");
        verify(existingUser).setUpdatedAt(any(OffsetDateTime.class));

        // Verify
        verify(repository).save(existingUser);
        verifyNoInteractions(eventProducer);
    }
}