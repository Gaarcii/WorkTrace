package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.shared.model.AuthenticatedUser;
import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link AuthenticatedUserPort} sobre Spring
 * Security.
 * <p>
 * Recupera el usuario autenticado a través del {@link UserService} y lo traduce
 * al modelo compartido {@link AuthenticatedUser}, aislando al dominio de los
 * detalles del contexto de seguridad y de las entidades JPA.
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityUserAdapter implements AuthenticatedUserPort {

    private final UserService userService;

    /**
     * {@inheritDoc}
     * <p>
     * Obtiene la información de usuario y empresa del contexto de seguridad y la
     * mapea a {@link AuthenticatedUser}.
     */
    @Override
    public AuthenticatedUser getAuthenticatedUser() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        return new AuthenticatedUser(
                info.getCompany().getId(),
                info.getProfile().getUserId(),
                info.getUser().getId()
        );
    }
}
