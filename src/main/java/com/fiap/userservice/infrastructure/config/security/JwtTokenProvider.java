package com.fiap.userservice.infrastructure.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * Gera e valida tokens JWT.
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;

    /**
     * Construtor: exige que o segredo seja uma string Base64 que decodifique
     * para ao menos 32 bytes (256 bits) para uso com HS256.
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long validityInMilliseconds) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be provided and Base64-encoded");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException("jwt.secret must be a valid Base64 string", ex);
        }

        if (decoded.length < 32) {
            throw new IllegalArgumentException("jwt.secret must decode to at least 32 bytes (256 bits) for HS256");
        }

        this.key = Keys.hmacShaKeyFor(decoded);
        this.validityInMilliseconds = validityInMilliseconds;
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