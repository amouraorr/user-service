package com.fiap.userservice.infrastructure.persistence.mapper;

import com.fiap.userservice.domain.model.User;
import com.fiap.userservice.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper() {
        @Override
        public UserEntity toEntity(User user) {
            return null;
        }

        @Override
        public User toDomain(UserEntity entity) {
            return null;
        }
    };

    @Test
    @DisplayName("mapLongToUuid_ReturnsUuid - Deve converter Long para UUID quando valor não for nulo")
    void mapLongToUuid_ReturnsUuid() {
        // Arrange
        Long value = 123L;
        // Act
        UUID result = mapper.map(value);
        // Assert
        assertNotNull(result);
        assertEquals(value.longValue(), result.getLeastSignificantBits());
    }

    @Test
    @DisplayName("mapLongToUuid_NullReturnsNull - Deve retornar null quando Long for nulo")
    void mapLongToUuid_NullReturnsNull() {
        // Arrange
        Long value = null;
        // Act
        UUID result = mapper.map(value);
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapUuidToLong_ReturnsLong - Deve converter UUID para Long quando UUID não for nulo")
    void mapUuidToLong_ReturnsLong() {
        // Arrange
        long lsb = 456L;
        UUID uuid = new UUID(0L, lsb);
        // Act
        Long result = mapper.map(uuid);
        // Assert
        assertNotNull(result);
        assertEquals(lsb, result.longValue());
    }

    @Test
    @DisplayName("mapUuidToLong_NullReturnsNull - Deve retornar null quando UUID for nulo")
    void mapUuidToLong_NullReturnsNull() {
        // Arrange
        UUID uuid = null;
        // Act
        Long result = mapper.map(uuid);
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("defaultMethodsSpy_VerifyInvocations - Verifica invocações usando Mockito.spy e que métodos abstratos não são chamados")
    void defaultMethodsSpy_VerifyInvocations() {
        // Arrange
        UserEntityMapper spyMapper = Mockito.spy(mapper);
        Long value = 789L;
        UUID uuid = new UUID(0L, 789L);

        // Act
        spyMapper.map(value);
        spyMapper.map(uuid);

        // Verify
        verify(spyMapper, times(1)).map(value);
        verify(spyMapper, times(1)).map(uuid);
        verify(spyMapper, never()).toEntity(any());
        verify(spyMapper, never()).toDomain(any());
    }
}