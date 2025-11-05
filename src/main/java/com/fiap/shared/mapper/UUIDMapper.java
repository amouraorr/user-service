/*
package com.fiap.shared.mapper;

import org.springframework.stereotype.Component;

import java.util.UUID;

*/
/**
 * Helper para conversões UUID <-> String.
 * MapStruct chamará esses métodos quando encontrar mismatch entre
 * UUID e String se o mapper declarar `uses = UUIDMapper.class`.
 *//*

@Component
public class UUIDMapper {

    public String asString(UUID id) {
        return id == null ? null : id.toString();
    }

    public UUID asUUID(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // opcional: lançar, logar ou retornar null dependendo do comportamento desejado
            throw new RuntimeException("UUID inválido: " + id, e);
        }
    }
}*/
