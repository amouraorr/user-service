package com.fiap.userservice.application.security;
/**
 * Interface para codificação e verificação de senhas.
 */
public interface AppPasswordEncoder {

    /**
     * Codifica a senha em texto plano.
     *
     * @param rawPassword senha em texto plano
     * @return valor codificado da senha
     */
    String encode(String rawPassword);

    /**
     * Verifica se a senha em texto plano corresponde à senha codificada.
     *
     * @param rawPassword     senha em texto plano
     * @param encodedPassword senha codificada
     * @return true se corresponder, false caso contrário
     */
    boolean matches(String rawPassword, String encodedPassword);
}