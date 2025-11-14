package com.fiap.userservice.infrastructure.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private Environment env;

    private static final long VALIDITY = 3600000L;

    @BeforeEach
    void beforeEach() {
        when(env.getActiveProfiles()).thenReturn(new String[]{});
    }

    @Test
    @DisplayName("Quando secret está em Base64 com pelo menos 32 bytes, deve criar e validar token com role")
    void shouldCreateAndValidateTokenWithBase64Secret() {
        // Arrange
        byte[] keyBytes = new byte[32];
        new Random().nextBytes(keyBytes);
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        JwtTokenProvider provider = new JwtTokenProvider(base64Secret, VALIDITY, env);

        // Act
        String token = provider.createToken("user1", "ADMIN");

        // Assert
        assertTrue(provider.validateToken(token));
        assertEquals("user1", provider.getUsername(token));
        assertEquals("ADMIN", provider.getRole(token));

        // Verify
        verify(env, atLeastOnce()).getActiveProfiles();
    }

    @Test
    @DisplayName("Quando secret é plain-text com pelo menos 32 caracteres, deve criar e validar token com role")
    void shouldCreateAndValidateTokenWithPlainTextLongSecret() {
        // Arrange
        String longSecret = "this-is-a-plain-text-secret-with-more-than-32-chars-12345";
        JwtTokenProvider provider = new JwtTokenProvider(longSecret, VALIDITY, env);

        // Act
        String token = provider.createToken("user2", "USER");

        // Assert
        assertTrue(provider.validateToken(token));
        assertEquals("user2", provider.getUsername(token));
        assertEquals("USER", provider.getRole(token));

        // Verify
        verify(env, atLeastOnce()).getActiveProfiles();
    }

    @Test
    @DisplayName("Quando secret é curto, deve derivar chave via SHA-256 e criar token válido")
    void shouldDeriveKeyWhenSecretTooShort() {
        // Arrange
        String shortSecret = "short-secret";
        JwtTokenProvider provider = new JwtTokenProvider(shortSecret, VALIDITY, env);

        // Act
        String token = provider.createToken("user3", "MANAGER");

        // Assert
        assertTrue(provider.validateToken(token));
        assertEquals("user3", provider.getUsername(token));
        assertEquals("MANAGER", provider.getRole(token));

        // Verify
        verify(env, atLeastOnce()).getActiveProfiles();
    }

    @Test
    @DisplayName("Quando profile 'prod' ativo e secret ausente, deve lançar IllegalArgumentException")
    void shouldThrowWhenSecretMissingAndProdProfileActive() {
        // Arrange
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenProvider("", VALIDITY, env));

        assertTrue(ex.getMessage().contains("jwt.secret") || ex.getMessage().contains("security.jwt.secret"));

        // Verify
        verify(env, atLeastOnce()).getActiveProfiles();
    }

    @Test
    @DisplayName("Quando token criado sem role, getRole deve retornar null")
    void shouldReturnNullRoleWhenNoRoleInToken() {
        // Arrange
        String longSecret = "another-plain-text-secret-that-is-long-enough-123456";
        JwtTokenProvider provider = new JwtTokenProvider(longSecret, VALIDITY, env);

        // Act
        String token = provider.createToken("user4", null);

        // Assert
        assertTrue(provider.validateToken(token));
        assertEquals("user4", provider.getUsername(token));
        assertNull(provider.getRole(token));

        // Verify
        verify(env, atLeastOnce()).getActiveProfiles();
    }
}