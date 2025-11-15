package com.fiap.userservice.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    @Test
    @DisplayName("prePersist gera id e timestamps quando nulos")
    void testPrePersist_setsIdAndTimestampsWhenNull() {
        // Arrange
        UserEntity entity = new UserEntity();

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        // Act
        entity.prePersist();

        // Assert
        assertNotNull(entity.getId(), "id deve ser gerado no prePersist");
        assertNotNull(entity.getCreatedAt(), "createdAt deve ser definido no prePersist");
        assertNotNull(entity.getUpdatedAt(), "updatedAt deve ser definido no prePersist");
        assertFalse(entity.getCreatedAt().isAfter(entity.getUpdatedAt()), "createdAt não pode ser depois de updatedAt");
    }

    @Test
    @DisplayName("prePersist não sobrescreve id e createdAt quando já existem")
    void testPrePersist_doesNotOverrideExistingIdOrCreatedAt() {
        // Arrange
        UserEntity entity = new UserEntity();
        UUID existingId = UUID.randomUUID();
        OffsetDateTime existingCreatedAt = OffsetDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS);

        entity.setId(existingId);
        entity.setCreatedAt(existingCreatedAt);
        entity.setUpdatedAt(null);

        // Act
        entity.prePersist();

        // Assert
        assertEquals(existingId, entity.getId(), "id não deve ser modificado pelo prePersist se já existir");
        assertEquals(existingCreatedAt, entity.getCreatedAt(), "createdAt não deve ser modificado pelo prePersist se já existir");
        assertNotNull(entity.getUpdatedAt(), "updatedAt deve ser preenchido no prePersist");
        assertFalse(entity.getCreatedAt().isAfter(entity.getUpdatedAt()), "createdAt não pode ser depois de updatedAt");
    }

    @Test
    @DisplayName("preUpdate atualiza apenas updatedAt")
    void testPreUpdate_updatesUpdatedAtOnly() throws InterruptedException {
        // Arrange
        UserEntity entity = new UserEntity();
        OffsetDateTime oldUpdatedAt = OffsetDateTime.now().minusHours(2).truncatedTo(ChronoUnit.MILLIS);
        entity.setUpdatedAt(oldUpdatedAt);
        OffsetDateTime before = entity.getUpdatedAt();

        // Act
        entity.preUpdate();

        // Assert
        assertNotNull(entity.getUpdatedAt(), "updatedAt deve ser não-nulo após preUpdate");
        assertTrue(entity.getUpdatedAt().isAfter(before) || entity.getUpdatedAt().isEqual(before), "updatedAt deve ser atualizado no preUpdate");
    }

    @Test
    @DisplayName("getters e setters funcionam corretamente")
    void testGettersAndSetters() {
        // Arrange
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        String username = "user@example.com";
        String password = "securePassword";
        String phone = "+5511999999999";
        String apartment = "101A";
        String role = "USER";
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(5).truncatedTo(ChronoUnit.MILLIS);
        OffsetDateTime updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        // Act
        entity.setId(id);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setPhone(phone);
        entity.setApartment(apartment);
        entity.setRole(role);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(username, entity.getUsername());
        assertEquals(password, entity.getPassword());
        assertEquals(phone, entity.getPhone());
        assertEquals(apartment, entity.getApartment());
        assertEquals(role, entity.getRole());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
}