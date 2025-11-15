package com.fiap.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "jwt.secret=ZmFrZS1zZWNyZXQta2V5LXRlc3QtZm9yLXVuaXR0",
        "jwt.expirationMs=3600000"
})
@Import(UserServiceApplicationTests.TestSecurityConfig.class)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Bean
        public JwtDecoder jwtDecoder() {
            byte[] keyBytes;
            try {
                // tenta decodificar como base64 (o método que sua produção usa)
                keyBytes = Base64.getDecoder().decode(jwtSecret);
            } catch (IllegalArgumentException ex) {
                // se não for base64 válido, usa os bytes da string (fallback)
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }

            SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            return NimbusJwtDecoder.withSecretKey(secretKey).build();
        }
    }
}