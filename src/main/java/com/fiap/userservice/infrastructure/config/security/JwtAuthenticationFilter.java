package com.fiap.userservice.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro que valida JWT no header Authorization: Bearer <token>
 * Preenche SecurityContext com UsernamePasswordAuthenticationToken.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsername(token);
                    String role = jwtTokenProvider.getRole(token);
                    Collection<SimpleGrantedAuthority> authorities = List.of(role == null ? "USER" : role)
                            .stream()
                            .map(r -> {
                                String roleName = r.startsWith("ROLE_") ? r : ("ROLE_" + r);
                                return new SimpleGrantedAuthority(roleName);
                            })
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ex) {
                // se token inválido, limpar contexto e deixar seguir para que a segurança rejeite
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}