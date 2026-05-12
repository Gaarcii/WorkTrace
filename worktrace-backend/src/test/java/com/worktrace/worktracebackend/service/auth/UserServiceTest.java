package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Company testCompany;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa de Prueba")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("password")
                .company(testCompany)
                .build();

        testProfile = Profile.builder()
                .userId(testUser.getId())
                .fullName("Usuario de Prueba")
                .user(testUser)
                .build();
        
        testUser.setProfile(testProfile);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testUserDetailsServiceSuccess() {
        String userEmail = "test@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        UserDetailsService userDetailsService = userService.userDetailsService();

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        assertAll("Carga de usuario por email",
                () -> assertNotNull(userDetails, "Los detalles del usuario no deberían ser nulos"),
                () -> assertEquals(userEmail, userDetails.getUsername(), "El email del usuario no coincide")
        );
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void testUserDetailsServiceNotFound() {
        String nonExistentEmail = "noexiste@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());
        UserDetailsService userDetailsService = userService.userDetailsService();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userDetailsService.loadUserByUsername(nonExistentEmail), "Debería lanzarse una excepción NotFoundException");

        assertEquals("Usuario no encontrado con email: " + nonExistentEmail, exception.getMessage());
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void testGetAuthenticatedUserSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        User authenticatedUser = userService.getAuthenticatedUser();

        assertAll("Obtención de usuario autenticado",
                () -> assertNotNull(authenticatedUser, "El usuario autenticado no debería ser nulo"),
                () -> assertEquals(testUser.getId(), authenticatedUser.getId(), "El ID del usuario no coincide")
        );
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void testGetAuthenticatedUserNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getAuthenticatedUser());

        assertEquals("No se ha encontrado el usuario autenticado", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testGetAuthenticatedUserAndCompanyInfoSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        assertAll("Información de usuario y compañía",
                () -> assertNotNull(info, "La información no debería ser nula"),
                () -> assertEquals(testUser, info.getUser(), "El usuario no coincide"),
                () -> assertEquals(testCompany, info.getCompany(), "La compañía no coincide"),
                () -> assertEquals(testProfile, info.getProfile(), "El perfil no coincide")
        );
    }

    @Test
    void testGetUserByIdSuccess() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(userId);

        assertNotNull(foundUser, "El usuario encontrado no debería ser nulo");
        assertEquals(userId, foundUser.getId(), "El ID del usuario no coincide");
        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(nonExistentId));

        assertEquals("Usuario no encontrado con email: " + nonExistentId, exception.getMessage());
        verify(userRepository).findById(nonExistentId);
    }
}
