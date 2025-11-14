package com.fiap.userservice.application.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimpleAppPasswordEncoderTest {


    @Test
    @DisplayName("encode gera string no formato iterations:salt:hash")
    void testEncodeReturnsCorrectFormat() {
        // Arrange
        SimpleAppPasswordEncoder encoder = new SimpleAppPasswordEncoder();
        String raw = "myStrongP@ssw0rd";

        // Act
        String encoded = encoder.encode(raw);

        // Assert
        assertNotNull(encoded, "encoded não deve ser nulo");
        String[] parts = encoded.split(":");
        assertEquals(3, parts.length, "encoded deve ter 3 partes separadas por ':'");

        // iterations
        int iterations = Integer.parseInt(parts[0]);
        assertEquals(65536, iterations, "iterations deve ser 65536 (DEFAULT_ITERATIONS)");

        // salt
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        assertEquals(16, salt.length, "salt deve ter 16 bytes (SALT_LENGTH)");

        // hash
        byte[] hash = Base64.getDecoder().decode(parts[2]);
        assertEquals(32, hash.length, "hash deve ter 32 bytes (KEY_LENGTH / 8)");
    }

    @Test
    @DisplayName("matches retorna true para senha válida")
    void testMatchesReturnsTrueForValidPassword() {
        // Arrange
        SimpleAppPasswordEncoder encoder = new SimpleAppPasswordEncoder();
        String raw = "correctHorseBatteryStaple";

        // Act
        String encoded = encoder.encode(raw);
        boolean result = encoder.matches(raw, encoded);

        // Assert
        assertTrue(result, "matches deve retornar true para a senha correta");
    }

    @Test
    @DisplayName("matches retorna false para senha inválida")
    void testMatchesReturnsFalseForDifferentPassword() {
        // Arrange
        SimpleAppPasswordEncoder encoder = new SimpleAppPasswordEncoder();
        String raw = "passwordOne";
        String wrong = "passwordTwo";

        // Act
        String encoded = encoder.encode(raw);
        boolean result = encoder.matches(wrong, encoded);

        // Assert
        assertFalse(result, "matches deve retornar false para senha incorreta");
    }

    @Test
    @DisplayName("matches retorna false quando input é nulo")
    void testMatchesReturnsFalseForNullInputs() {
        // Arrange
        SimpleAppPasswordEncoder encoder = new SimpleAppPasswordEncoder();
        String raw = "somePassword";

        // Act
        String encoded = encoder.encode(raw);

        // Assert
        assertFalse(encoder.matches(null, encoded), "matches deve retornar false quando rawPassword é nulo");
        assertFalse(encoder.matches(raw, null), "matches deve retornar false quando encodedPassword é nulo");
    }

    @Test
    @DisplayName("matches retorna false para formatos inválidos de encodedPassword")
    void testMatchesReturnsFalseForMalformedEncodedPassword() {
        // Arrange
        SimpleAppPasswordEncoder encoder = new SimpleAppPasswordEncoder();
        String raw = "anotherPassword";

        // Act & Assert
        assertFalse(encoder.matches(raw, "badformat"), "matches deve retornar false para encoded sem 3 partes");

        // iterations não numérico
        String fakeSalt = Base64.getEncoder().encodeToString(new byte[16]);
        String fakeHash = Base64.getEncoder().encodeToString(new byte[32]);
        String invalidIterations = "notAnInt:" + fakeSalt + ":" + fakeHash;
        assertFalse(encoder.matches(raw, invalidIterations), "matches deve retornar false quando iterations não é inteiro");
    }
}