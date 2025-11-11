package com.fiap.userservice.infrastructure.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuração de segurança específica para o profile 'docker' do User Service.
 *
 * Neste arquivo mantemos:
 * - O endpoint de criação de usuário (/api/internal/users/**) aberto para permitir cadastro sem login.
 * - O endpoint de login de teste (/api/internal/auth/login) aberto para gerar tokens via Postman.
 * - Todas as demais rotas exigem autenticação JWT. Rotas específicas são restritas por ROLE.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("docker")
public class SecurityConfigDocker {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigDocker.class);

    @Bean
    @Order(1)
    public SecurityFilterChain userServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {
        logger.info("Registrando SecurityFilterChain para profile 'docker' com ordem=1 (ambiente de testes).");

        // Importante: definimos explicitamente um securityMatcher com padrão Ant ("/**").
        http.securityMatcher("/**");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permitir Swagger / OpenAPI / recursos estáticos
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/api-doc/**",
                                "/webjars/**",
                                "/favicon.ico").permitAll()

                        // Opcional: expor health/info sem autenticação
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()

                        // Permite criação de usuários (morador/porteiro) sem autenticação
                        .requestMatchers(HttpMethod.POST, "/api/internal/users/**").permitAll()
                        // Permitir endpoint de login interno para geração de tokens (apenas para dev)
                        .requestMatchers(HttpMethod.POST, "/api/internal/auth/login").permitAll()

                        // Endpoints que só o porteiro pode executar
                        .requestMatchers(HttpMethod.POST, "/api/parcels").hasRole("PORTEIRO")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/*/pickup").hasRole("PORTEIRO")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/*/confirm").hasRole("MORADOR")

                        // Qualquer outra requisição requer autenticação
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    /**
     * JwtDecoder HS256 (Nimbus) usando segredo Base64 ou texto simples como fallback.
     * Aceita tanto 'security.jwt.secret' quanto 'jwt.secret' (prefere security.jwt.secret).
     * Se a propriedade não for informada, o decoder lançará JwtException.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:${jwt.secret:}}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.warn("security.jwt.secret / jwt.secret não definidos; JwtDecoder retornará erro ao decodificar tokens.");
            return token -> {
                throw new JwtException("JwtDecoder não configurado para o profile 'docker'. Defina 'security.jwt.secret' ou 'jwt.secret' para habilitar validação de tokens.");
            };
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtSecret);
            logger.info("JwtDecoder configurado com segredo fornecido como Base64 ({} bytes).", keyBytes.length);
        } catch (IllegalArgumentException ex) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            logger.warn("Propriedade 'security.jwt.secret'/'jwt.secret' não é um Base64 válido; usando bytes UTF-8 do valor fornecido ({} bytes).", keyBytes.length);
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Converte claim 'roles' em GrantedAuthority com prefixo ROLE_.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(r -> "ROLE_" + r)
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return conv;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}