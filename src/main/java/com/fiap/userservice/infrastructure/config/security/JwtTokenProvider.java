package com.fiap.userservice.infrastructure.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

/**
 * Gera e valida tokens JWT.
 *
 * Ajustado para aceitar tanto secrets em Base64 quanto plain-text (útil em ambiente local/docker).
 * Se o valor fornecido não for Base64 válido com pelo menos 32 bytes, derivamos 32 bytes via SHA-256
 * a partir do texto fornecido para garantir comprimento mínimo para HS256.
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;

    /**
     * Construtor: aceita segredo em Base64 ou plain-text.
     *
     * @param secret                    segredo fornecido via propriedade jwt.secret (pode ser Base64 ou texto simples)
     * @param validityInMilliseconds    tempo de validade em ms
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long validityInMilliseconds) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be provided (Base64 or plain text)");
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
     */
    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
     */
    public String getRole(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }
}