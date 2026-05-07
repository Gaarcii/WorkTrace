package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para gestionar operaciones relacionadas con los usuarios.
 * Proporciona funcionalidades para obtener información del usuario autenticado,
 * así como para integrarse con el sistema de seguridad de Spring.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Proporciona una implementación de UserDetailsService para Spring Security.
     * Permite a Spring Security cargar los detalles de un usuario por su email (username).
     * @return La implementación de UserDetailsService.
     */
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + username));
    }

    /**
     * Obtiene la entidad User del usuario actualmente autenticado en el sistema.
     * Es un método de utilidad para acceder rápidamente al usuario que realiza la petición.
     * @return La entidad User del usuario autenticado.
     */
    public User getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No se ha encontrado el usuario autenticado");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Obtiene un objeto que contiene la información combinada del usuario, su empresa y su perfil.
     * Simplifica el acceso a datos relacionados del usuario autenticado.
     * @return Un objeto UserAndCompanyInfo con la información del usuario, empresa y perfil.
     */
    public UserAndCompanyInfo getAuthenticatedUserAndCompanyInfo() {
        User user = this.getAuthenticatedUser();
        Company company = user.getCompany();
        Profile profile = user.getProfile();

        return new UserAndCompanyInfo(user, company, profile);
    }

    /**
     * Busca y devuelve un usuario por su identificador único (UUID).
     * @param id El UUID del usuario a buscar.
     * @return La entidad User correspondiente al ID proporcionado.
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + id));
    }

}
