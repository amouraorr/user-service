package com.fiap.userservice.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    @DisplayName("Retorna BCryptPasswordEncoder ao solicitar o bean de PasswordEncoder")
    void shouldReturnBCryptPasswordEncoder() {
        // Arrange
        SecurityConfig config = new SecurityConfig();

        // Act
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder, "Deve ser uma instância de BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("Cria UserDetailsService com usuário 'fiap' usando PasswordEncoder fornecido")
    void shouldCreateInMemoryUserWithEncodedPassword() {
        // Arrange
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
        Mockito.when(mockEncoder.encode("fiap2025")).thenReturn("encodedPass");

        // Act
        UserDetailsService uds = config.userDetailsService(mockEncoder);

        // Assert
        assertNotNull(uds);
        assertTrue(uds instanceof InMemoryUserDetailsManager, "Deve retornar InMemoryUserDetailsManager");

        UserDetails user = uds.loadUserByUsername("fiap");
        assertNotNull(user, "Usuário 'fiap' deve existir");
        assertEquals("encodedPass", user.getPassword(), "Senha deve ser a senha codificada pelo encoder mockado");
        assertTrue(user.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())),
                "Usuário deve ter a role ROLE_USER");

        // Verify
        Mockito.verify(mockEncoder).encode("fiap2025");
    }

    @Test
    @DisplayName("Senha do usuário é compatível com BCrypt gerado pelo bean")
    void shouldEncodePasswordWithBCrypt() {
        // Arrange
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        // Act
        UserDetailsService uds = config.userDetailsService(encoder);
        UserDetails user = uds.loadUserByUsername("fiap");

        // Assert
        assertNotNull(user);
        assertTrue(encoder.matches("fiap2025", user.getPassword()), "O encoder BCrypt deve validar a senha raw com a codificada");
    }
}