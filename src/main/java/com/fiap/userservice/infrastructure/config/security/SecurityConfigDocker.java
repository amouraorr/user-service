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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Configuração de segurança específica para o profile 'docker' do User Service.
 *
 * Ajustes:
 * - JwtDecoder agora resolve os bytes da chave da mesma forma que o JwtTokenProvider
 *   (Base64 -> UTF-8 -> SHA-256), evitando falha de validação por chaves inconsistentes.
 * - JwtAuthenticationConverter aceita tanto 'roles' (lista) quanto 'role' (string) como fonte de authorities.
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
     * Consolida a mesma lógica de derivação de bytes da chave usada no JwtTokenProvider:
     *  - tenta Base64 (se >= 32 bytes)
     *  - senão usa bytes UTF-8 (se >= 32 bytes)
     *  - senão deriva 32 bytes via SHA-256 do texto
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:${jwt.secret:}}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.warn("security.jwt.secret / jwt.secret não definidos; JwtDecoder retornará erro ao decodificar tokens.");
            return token -> {
                throw new JwtException("JwtDecoder não configurado para o profile 'docker'. Defina 'security.jwt.secret' ou 'jwt.secret' para habilitar validação de tokens.");
            };
        }

        byte[] keyBytes = resolveKeyBytes(jwtSecret);

        logger.info("JwtDecoder configurado com {} bytes de chave para HMAC.", keyBytes.length);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Converte claim 'roles' em GrantedAuthority com prefixo ROLE_.
     * Também aceita claim 'role' que pode ser string única ou lista em string separada por vírgula.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                // fallback: claim 'role' pode ser string única ou texto "A,B"
                Object roleObj = jwt.getClaims().get("role");
                if (roleObj instanceof String) {
                    String roleStr = (String) roleObj;
                    roles = Arrays.stream(roleStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            }

            if (roles == null || roles.isEmpty()) {
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

    /**
     * Resolve bytes para a chave HMAC:
     * - Tenta decodificar Base64; se resultar em >= 32 bytes, usa esse valor.
     * - Caso contrário, usa os bytes UTF-8 do texto; se tiver >= 32 bytes, usa diretamente.
     * - Caso contrário, aplica SHA-256 no texto para obter 32 bytes.
     *
     * (mesma lógica do JwtTokenProvider para compatibilidade)
     */
    private byte[] resolveKeyBytes(String secret) {
        // tenta decodificar Base64
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // não era Base64 -> prosseguir
        }

        // usa bytes UTF-8
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) {
            return raw;
        }

        // derivar 32 bytes via SHA-256
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(raw); // 32 bytes
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available to derive JWT key bytes", e);
        }
    }
}