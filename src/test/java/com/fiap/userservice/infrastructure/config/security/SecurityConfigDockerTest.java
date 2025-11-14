package com.fiap.userservice.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigDockerTest {

    private final SecurityConfigDocker config = new SecurityConfigDocker();

    @Test
    @DisplayName("resolveKeyBytes - quando segredo é Base64 com >=32 bytes deve retornar bytes decodificados")
    void resolveKeyBytes_whenBase64Long_thenReturnDecodedBytes() throws Exception {
        // Arrange
        byte[] original = new byte[32];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) i;
        }
        String base64 = Base64.getEncoder().encodeToString(original);

        // Act
        Method m = SecurityConfigDocker.class.getDeclaredMethod("resolveKeyBytes", String.class);
        m.setAccessible(true);
        byte[] result = (byte[]) m.invoke(config, base64);

        // Assert
        assertArrayEquals(original, result);
    }

    @Test
    @DisplayName("resolveKeyBytes - quando segredo é texto com >=32 bytes deve retornar bytes UTF-8")
    void resolveKeyBytes_whenRawUtf8Long_thenReturnRawBytes() throws Exception {
        // Arrange
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append("a");
        }
        String secret = sb.toString();

        // Act
        Method m = SecurityConfigDocker.class.getDeclaredMethod("resolveKeyBytes", String.class);
        m.setAccessible(true);
        byte[] result = (byte[]) m.invoke(config, secret);

        // Assert
        assertArrayEquals(secret.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    @DisplayName("resolveKeyBytes - quando segredo curto deve derivar 32 bytes via SHA-256")
    void resolveKeyBytes_whenShort_thenReturnSha256Digest() throws Exception {
        // Arrange
        String secret = "short-secret";

        // Act
        Method m = SecurityConfigDocker.class.getDeclaredMethod("resolveKeyBytes", String.class);
        m.setAccessible(true);
        byte[] result = (byte[]) m.invoke(config, secret);

        // Assert
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] expected = md.digest(secret.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(expected, result);
        assertEquals(32, result.length);
    }

    @Test
    @DisplayName("jwtDecoder - quando segredo em branco deve retornar JwtDecoder que lança JwtException ao decodificar")
    void jwtDecoder_whenSecretBlank_thenDecoderThrowsJwtException() {
        // Arrange
        String blank = "   ";

        // Act
        JwtDecoder decoder = config.jwtDecoder(blank);

        // Assert
        assertNotNull(decoder);
        assertThrows(JwtException.class, () -> {
            // Verify
            decoder.decode("any-token");
        });
    }

    @Test
    @DisplayName("jwtDecoder - quando segredo Base64 válido deve retornar JwtDecoder não-nulo")
    void jwtDecoder_whenValidSecret_thenReturnDecoder() {
        // Arrange
        byte[] original = new byte[40];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) (i + 1);
        }
        String base64 = Base64.getEncoder().encodeToString(original);

        // Act
        JwtDecoder decoder = config.jwtDecoder(base64);

        // Assert
        assertNotNull(decoder);
    }

    @Test
    @DisplayName("jwtAuthenticationConverter - quando claim 'roles' é lista deve converter para ROLE_...")
    void jwtAuthenticationConverter_whenRolesList_thenConvertToRoleAuthorities() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN", "USER"))
                .build();

        // Act
        Method m = SecurityConfigDocker.class.getDeclaredMethod("jwtAuthenticationConverter");
        m.setAccessible(true);
        JwtAuthenticationConverter conv = (JwtAuthenticationConverter) m.invoke(config);

        Authentication auth = conv.convert(jwt);

        // Assert
        assertNotNull(auth);
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), authorities);
    }

    @Test
    @DisplayName("jwtAuthenticationConverter - quando claim 'role' é string separada por vírgula deve converter para ROLE_...")
    void jwtAuthenticationConverter_whenRoleStringCommaSeparated_thenConvertToRoleAuthorities() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("role", "ADMIN,USER")
                .build();

        // Act
        Method m = SecurityConfigDocker.class.getDeclaredMethod("jwtAuthenticationConverter");
        m.setAccessible(true);
        JwtAuthenticationConverter conv = (JwtAuthenticationConverter) m.invoke(config);

        Authentication auth = conv.convert(jwt);

        // Assert
        assertNotNull(auth);
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), authorities);
    }

    @Test
    @DisplayName("passwordEncoder - deve retornar BCryptPasswordEncoder funcional")
    void passwordEncoder_returnsBCrypt() {
        // Arrange
        PasswordEncoder encoder = config.passwordEncoder();

        // Act
        String raw = "my-secret";
        String encoded = encoder.encode(raw);

        // Assert
        assertTrue(encoder instanceof BCryptPasswordEncoder);
        assertTrue(encoder.matches(raw, encoded));
    }
}