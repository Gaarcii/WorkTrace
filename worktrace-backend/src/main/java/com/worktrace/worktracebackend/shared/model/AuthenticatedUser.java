package com.worktrace.worktracebackend.shared.model;

import java.util.UUID;

/**
 * Modelo compartido que representa al usuario autenticado en el contexto de la
 * petición actual.
 * <p>
 * Es un objeto de solo lectura que transporta los identificadores que necesitan
 * los casos de uso para resolver el usuario y aplicar el aislamiento
 * multi-tenant, sin acoplarse a las entidades JPA ni al contexto de seguridad
 * de Spring.
 *
 * @param companyId     Identificador de la empresa a la que pertenece el usuario.
 * @param profileUserId Identificador del perfil del usuario (clave de las
 *                      relaciones de perfil, p. ej. en los fichajes).
 */
public record AuthenticatedUser(
        UUID companyId,
        UUID profileUserId
) {
}
