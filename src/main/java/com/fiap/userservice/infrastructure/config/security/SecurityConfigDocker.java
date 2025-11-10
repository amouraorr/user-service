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
 * ATENÇÃO: Este arquivo relaxa a segurança (permitAll / comentário do oauth2ResourceServer)
 * para permitir testes locais via docker-compose. Em produção remova este profile e
 * reative a validação JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("docker")
public class SecurityConfigDocker {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigDocker.class);

    /**
     * SecurityFilterChain para ambiente docker: libera endpoints de criação de usuários
     * (incluindo porteiro) e, por conveniência de testes locais, libera todas as requisições.
     *
     * Para reativar segurança, restaure as regras comentadas e remova o profile 'docker'.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain userServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {
        logger.info("Registrando SecurityFilterChain para profile 'docker' com ordem=1 (ambiente de testes).");

        // Importante: definimos explicitamente um securityMatcher com padrão Ant ("/**").
        // Isso evita registrar um filter chain com AnyRequestMatcher (que causaria conflito
        // caso outra configuração também utilize anyRequest()). O validator do Spring Security
        // só considera "any request" como conflitante — usando um Ant matcher diferenciam-se os matchers.
        http.securityMatcher("/**");

        http
                // Desabilitamos CSRF para APIs stateless em testes locais
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permite explicitamente criação de usuários internos (ex.: porteiro)
                        .requestMatchers(HttpMethod.POST, "/api/internal/users/**").permitAll()
                        // Permite endpoint de login interno caso exista (ajuste conforme seu endpoint real)
                        .requestMatchers(HttpMethod.POST, "/api/internal/auth/login").permitAll()
                        // Para facilitar testes locais via docker, liberamos TODAS as requisições.
                        // Em produção, substitua por regras mais restritas e reative oauth2ResourceServer.
                        .anyRequest().permitAll()
                );

        // Comentado para testes docker: validação JWT reativar em produção.
        /*
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );
        */

        return http.build();
    }

    /**
     * JwtDecoder HS256 (Nimbus) usando segredo Base64 ou texto simples como fallback.
     * Se a propriedade 'security.jwt.secret' não for informada, este bean retornará um decoder
     * que lança JwtException ao tentar decodificar, evitando comportamento silencioso.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:}") String jwtSecretBase64) {
        if (jwtSecretBase64 == null || jwtSecretBase64.isBlank()) {
            logger.warn("security.jwt.secret não definido; JwtDecoder retornará erro ao decodificar tokens.");
            return token -> {
                throw new JwtException("JwtDecoder não configurado para o profile 'docker'. Defina 'security.jwt.secret' para habilitar validação de tokens.");
            };
        }

        byte[] keyBytes;
        try {
            // tenta interpretar como Base64
            keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
            logger.info("JwtDecoder configurado com segredo fornecido como Base64 ({} bytes).", keyBytes.length);
        } catch (IllegalArgumentException ex) {
            // fallback para UTF-8
            keyBytes = jwtSecretBase64.getBytes(StandardCharsets.UTF_8);
            logger.warn("Propriedade 'security.jwt.secret' não é um Base64 válido; usando bytes UTF-8 do valor fornecido ({} bytes).", keyBytes.length);
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Converte claim 'roles' em GrantedAuthority com prefixo ROLE_ (mantido para reativação futura).
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