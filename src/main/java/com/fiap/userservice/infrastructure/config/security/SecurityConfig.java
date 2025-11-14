package com.fiap.userservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para permitir acesso público ao Swagger e definir
 * um usuário em memória para testes (desenvolvimento).
 */
@Configuration
@Profile("dev")
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // liberar endpoints públicos
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/internal/auth/login").permitAll()
                        .requestMatchers("/api/internal/users/morador").permitAll()
                        .requestMatchers("/api/internal/users/porteiro").permitAll()
                        // permitir TODOS os métodos para endpoints de API (GET, POST, etc.)
                        .requestMatchers("/api/**").permitAll()
                        // qualquer outro request precisa de autenticação
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder()
                .username("fiap")
                .password(encoder.encode("fiap2025"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}