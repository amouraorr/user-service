package com.fiap.userservice.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Quando não há header Authorization, não deve autenticar e deve continuar o filtro")
    void shouldNotAuthenticateWhenNoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Quando header não começa com Bearer, não deve autenticar e deve continuar o filtro")
    void shouldNotAuthenticateWhenHeaderDoesNotContainBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Token abcdefg");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Quando token válido e role sem prefixo, deve autenticar com ROLE_ prefixado")
    void shouldAuthenticateValidTokenWithRoleWithoutPrefix() throws ServletException, IOException {
        // Arrange
        String token = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("usuario1");
        when(jwtTokenProvider.getRole(token)).thenReturn("ADMIN");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("usuario1", auth.getName());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, times(1)).validateToken(token);
        verify(jwtTokenProvider, times(1)).getUsername(token);
        verify(jwtTokenProvider, times(1)).getRole(token);
    }

    @Test
    @DisplayName("Quando token válido e role já com prefixo ROLE_, deve autenticar preservando o prefixo")
    void shouldAuthenticateValidTokenWithRoleWithPrefix() throws ServletException, IOException {
        // Arrange
        String token = "valid.token.rolepref";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("usuario2");
        when(jwtTokenProvider.getRole(token)).thenReturn("ROLE_MANAGER");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("usuario2", auth.getName());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));

        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Quando token válido e role é nulo, deve autenticar com ROLE_USER por padrão")
    void shouldAuthenticateWithDefaultUserRoleWhenRoleIsNull() throws ServletException, IOException {
        // Arrange
        String token = "valid.token.nullrole";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("usuario3");
        when(jwtTokenProvider.getRole(token)).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("usuario3", auth.getName());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Quando JwtTokenProvider lançar exceção, deve limpar contexto e continuar o filtro")
    void shouldClearContextWhenProviderThrowsException() throws ServletException, IOException {
        // Arrange
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("token inválido"));

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, times(1)).validateToken(token);
    }
}