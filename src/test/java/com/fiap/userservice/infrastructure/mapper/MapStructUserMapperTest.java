package com.fiap.userservice.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapStructUserMapperTest {

    private final MapStructUserMapper mapper = Mappers.getMapper(MapStructUserMapper.class);

    @Test
    @DisplayName("Deve retornar nulo ao mapear Long nulo para UUID")
    void mapNullLong_shouldReturnNullUuid() {
        // Arrange
        Long input = null;

        // Act
        UUID result = mapper.map(input);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar nulo ao mapear UUID nulo para Long")
    void mapNullUuid_shouldReturnNullLong() {
        // Arrange
        UUID input = null;

        // Act
        Long result = mapper.map(input);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve mapear Long para UUID preservando os least significant bits")
    void mapLongToUuid_shouldReturnUuidWithCorrectLeastSignificantBits() {
        // Arrange
        Long original = 1234567890123456789L;

        // Act
        UUID uuid = mapper.map(original);

        // Assert
        assertNotNull(uuid);
        assertEquals(0L, uuid.getMostSignificantBits());
        assertEquals(original, uuid.getLeastSignificantBits());
    }

    @Test
    @DisplayName("Deve mapear UUID para Long retornando os least significant bits")
    void mapUuidToLong_shouldReturnLeastSignificantBits() {
        // Arrange
        long lsb = -987654321098765432L;
        UUID input = new UUID(0L, lsb);

        // Act
        Long result = mapper.map(input);

        // Assert
        assertNotNull(result);
        assertEquals(lsb, result.longValue());
    }

    @Test
    @DisplayName("Deve retornar o mesmo valor após mapear Long -> UUID -> Long (round-trip)")
    void roundTripMapping_shouldReturnSameValue() {
        // Arrange
        Long original = Long.MIN_VALUE + 12345;

        // Act
        UUID uuid = mapper.map(original);
        Long roundTrip = mapper.map(uuid);

        // Assert
        assertNotNull(uuid);
        assertEquals(original, roundTrip);
    }
}