package com.fiap.userservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança para permitir acesso público ao Swagger e definir
 * um usuário em memória para testes (desenvolvimento).
 *
 * Atualizada para permitir:
 *  - POST /api/internal/users/morador
 *  - POST /api/internal/users/porteiro
 *  - POST /api/internal/auth/login
 *
 * E para registrar o filtro JWT (JwtAuthenticationFilter) que valida tokens Bearer.
 */
@Configuration
@Profile("!prod") // carrega sempre que NÃO houver o profile 'prod' ativo
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Define a cadeia de filtros de segurança.
     * Permitir endpoints de cadastro sem autenticação e registrar filtro JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger.json"
                        ).permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Permitir publicamente os endpoints de cadastro (POST)
                        .requestMatchers(HttpMethod.POST, "/api/internal/users/morador").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/internal/users/porteiro").permitAll()
                        // Permitir login publicamente
                        .requestMatchers(HttpMethod.POST, "/api/internal/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                // Mantém httpBasic (provoca o popup do navegador) e também o formLogin
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                // Registrando filtro JWT para validar Bearer tokens
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PasswordEncoder BCrypt para codificar senhas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Usuário em memória para testes de desenvolvimento.
     * Usuário: fiap / fiap2025
     * (usa InMemoryUserDetailsManager para ambientes de dev)
     */
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