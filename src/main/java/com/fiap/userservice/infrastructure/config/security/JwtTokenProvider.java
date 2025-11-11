package com.fiap.userservice.infrastructure.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;

/**
 * Gera e valida tokens JWT.
 *
 * Ajustado para aceitar tanto secrets em Base64 quanto plain-text (útil em ambiente local/docker).
 * Se o valor fornecido não for Base64 válido com pelo menos 32 bytes, derivamos 32 bytes via SHA-256
 * a partir do texto fornecido para garantir comprimento mínimo para HS256.
 *
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Key key;
    private final long validityInMilliseconds;

    /**
     * Construtor: lê preferencialmente 'security.jwt.secret' e usa 'jwt.secret' como fallback.
     * Exemplo de placeholder Spring usado: ${security.jwt.secret:${jwt.secret:}}
     */
    public JwtTokenProvider(@Value("${security.jwt.secret:${jwt.secret:}}") String secret,
                            @Value("${jwt.expiration-ms:3600000}") long validityInMilliseconds,
                            Environment env) {

        boolean prodActive = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if ((secret == null || secret.isBlank()) && prodActive) {
            // Em prod: exigimos explicitamente a propriedade
            throw new IllegalArgumentException("jwt.secret or security.jwt.secret must be provided (Base64 or plain text) when profile 'prod' is active");
        }

        if (secret == null || secret.isBlank()) {
            // Ambiente não-prod: gerar um secret derivado e logar WARN
            String fallback = "fiap-default-secret-" + UUID.randomUUID();
            log.warn("Property 'security.jwt.secret' or 'jwt.secret' is not set and profile 'prod' is NOT active. Using an auto-generated secret for development. Do NOT use this in production.");
            secret = fallback;
        }

        byte[] keyBytes = resolveKeyBytes(secret);

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * Resolve bytes para a chave HMAC:
     * - Tenta decodificar Base64; se resultar em >= 32 bytes, usa esse valor.
     * - Caso contrário, usa os bytes UTF-8 do texto; se tiver >= 32 bytes, usa diretamente.
     * - Caso contrário, aplica SHA-256 no texto para obter 32 bytes.
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

    /**
     * Cria um token JWT assinado com HS256.
     * Alteração: além do claim "role" (string), também adiciona o claim "roles" (lista) para compatibilidade
     * com conversores que esperam a claim plural.
     */
    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);

        // Adiciona também 'roles' como lista para que o JwtAuthenticationConverter dos serviços encontre as roles.
        if (role != null) {
            builder.claim("roles", List.of(role));
        }

        return builder.compact();
    }

    /**
     * Valida o token JWT.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extrai o nome de usuário (subject) do token.
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    /**
     * Extrai o claim "role" do token (se presente).
     *  - claim "role" (string)
     *  - claim "roles" (lista) e retorna o primeiro elemento
     */
    public String getRole(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        Object roleObj = claims.get("role");
        if (roleObj != null) {
            return roleObj.toString();
        }

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof java.util.List) {
            java.util.List<?> rolesList = (java.util.List<?>) rolesObj;
            if (!rolesList.isEmpty() && rolesList.get(0) != null) {
                return rolesList.get(0).toString();
            }
        } else if (rolesObj != null) {
            // se for string com vírgula ou similar, tentar retornar diretamente
            return rolesObj.toString();
        }

        return null;
    }
}