package com.fiap.userservice.application.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Implementação simples e segura de {@link AppPasswordEncoder} usando PBKDF2.
 *
 * Formato retornado por {@link #encode(String)}: iterations:saltBase64:hashBase64
 */
@Component
public class SimpleAppPasswordEncoder implements AppPasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes
    private final SecureRandom random = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        int iterations = DEFAULT_ITERATIONS;
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, iterations, KEY_LENGTH);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);
        return iterations + ":" + saltB64 + ":" + hashB64;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || rawPassword == null) return false;
        String[] parts = encodedPassword.split(":");
        if (parts.length != 3) return false;
        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

        byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | java.security.NoSuchAlgorithmException e) {
            // Em um projeto real, logar e tratar adequadamente.
            throw new IllegalStateException("Erro ao gerar hash da senha", e);
        }
    }
}