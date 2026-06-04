package com.worktrace.worktracebackend.shared.port;

import com.worktrace.worktracebackend.shared.model.AuthenticatedUser;

/**
 * Puerto compartido que abstrae el acceso al usuario autenticado.
 * <p>
 * Permite a los casos de uso del dominio conocer al usuario en curso (empresa,
 * perfil y cuenta) sin depender directamente de Spring Security. La
 * implementación reside en la capa de infraestructura.
 */
public interface AuthenticatedUserPort {

    /**
     * Devuelve el usuario autenticado en la petición actual.
     *
     * @return Los datos del usuario autenticado (empresa, perfil y cuenta).
     */
    AuthenticatedUser getAuthenticatedUser();
}
