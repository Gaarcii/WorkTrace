package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.user.AdminProfileRequestDto;
import com.worktrace.worktracebackend.dto.user.AdminRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.InspectorRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.dto.user.ProfileRequestDto;
import com.worktrace.worktracebackend.exception.InvalidCredentialsException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.*;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;
    @Mock
    private JobPositionRepository jobPositionRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @InjectMocks
    private AuthenticationService authenticationService;

    private CompanyRequestDto companyRequestDto;
    private User adminUser;
    private Company company;
    private JobPosition adminJobPosition;
    private Profile adminProfile;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(UUID.randomUUID())
                .companyName("Empresa Test S.L.")
                .cif("B12345678")
                .updatedAt(OffsetDateTime.now())
                .build();

        adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .passwordHash("hashedPassword")
                .role(Role.ADMIN)
                .isEnabled(true)
                .company(company)
                .build();

        adminJobPosition = JobPosition.builder()
                .id(UUID.randomUUID())
                .title("Jefe")
                .company(company)
                .build();

        adminProfile = Profile.builder()
                .userId(adminUser.getId())
                .fullName("Admin Principal")
                .employeeCode("A001")
                .phone("600111222")
                .position(adminJobPosition)
                .isFirstLogin(false)
                .isActive(true)
                .user(adminUser)
                .build();
        adminUser.setProfile(adminProfile);


        AdminProfileRequestDto adminProfileDto = new AdminProfileRequestDto("Admin Principal", "A001", "600111222");
        AdminRequestDto adminDto = new AdminRequestDto("admin@test.com", "password123", adminProfileDto);
        companyRequestDto = new CompanyRequestDto("Empresa Test S.L.", "B12345678", adminDto);
    }

    @Test
    void testRegisterCompanySuccess() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(jobPositionRepository.save(any(JobPosition.class))).thenReturn(adminJobPosition);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(adminProfile);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponseDto response = authenticationService.registerCompany(companyRequestDto);

        assertAll("Registro de empresa",
                () -> assertNotNull(response, "La respuesta no debería ser nula"),
                () -> assertEquals("jwt-token", response.getToken(), "El token JWT no es el esperado"),
                () -> assertFalse(response.getFirstLogin(), "El primer login debería ser falso para el admin"),
                () -> assertEquals(Role.ADMIN, response.getRole(), "El rol debería ser ADMIN")
        );

        verify(companyRepository).save(any(Company.class));
        verify(jobPositionRepository).save(any(JobPosition.class));
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        verify(passwordEncoder).encode("password123");
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void testSignInSuccess() {
        AuthRequestDto request = new AuthRequestDto("admin@test.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(adminUser)).thenReturn("jwt-token");

        AuthResponseDto response = authenticationService.signIn(request);

        assertAll("Inicio de sesión exitoso",
                () -> assertNotNull(response),
                () -> assertEquals("jwt-token", response.getToken()),
                () -> assertFalse(response.getFirstLogin()),
                () -> assertEquals(Role.ADMIN, response.getRole())
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("admin@test.com");
        verify(jwtService).generateToken(adminUser);
    }

    @Test
    void testSignInInvalidCredentials() {
        AuthRequestDto request = new AuthRequestDto("admin@test.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Bad credentials") {});

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authenticationService.signIn(request));

        assertEquals("Email o contraseña inválidos", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testChangePasswordSuccess() {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("currentPassword", "newPassword", "newPassword");
        UserAndCompanyInfo info = new UserAndCompanyInfo(adminUser, company,adminProfile);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(info);
        when(passwordEncoder.matches("currentPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authenticationService.changePassword(request);

        assertAll("Cambio de contraseña exitoso",
            () -> assertEquals("newHashedPassword", adminUser.getPasswordHash()),
            () -> assertFalse(adminUser.getProfile().getIsFirstLogin())
        );

        verify(userService).getAuthenticatedUserAndCompanyInfo();
        verify(passwordEncoder).matches("currentPassword", "hashedPassword");
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void testChangePasswordWrongCurrentPassword() {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("wrongPassword", "newPassword", "newPassword");
        UserAndCompanyInfo info = new UserAndCompanyInfo(adminUser, company, adminProfile);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(info);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authenticationService.changePassword(request));

        assertEquals("La contraseña actual es incorrecta o las nuevas no coinciden.", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testRegisterEmployeeSuccess() {
        UUID positionId = UUID.randomUUID();
        ProfileRequestDto profileDto = new ProfileRequestDto("Empleado Uno", "12345678A", "600222333", positionId, new BigDecimal("40"));
        EmployeeRequestDto employeeDto = new EmployeeRequestDto("empleado@test.com", profileDto, null);

        JobPosition employeePosition = JobPosition.builder().id(positionId).title("Desarrollador").company(company).build();

        when(userRepository.findByEmail("empleado@test.com")).thenReturn(Optional.empty());
        when(userService.getAuthenticatedUser()).thenReturn(adminUser);
        when(jobPositionRepository.findById(positionId)).thenReturn(Optional.of(employeePosition));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPasswordForEmployee");

        authenticationService.registerEmployee(employeeDto);

        verify(userRepository).saveAndFlush(any(User.class));
        verify(profileRepository).saveAndFlush(any(Profile.class));
        verify(emailService).sendNewEmployeeWelcomeEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterEmployeeEmailAlreadyExists() {
        EmployeeRequestDto employeeDto = new EmployeeRequestDto("empleado@existente.com", new ProfileRequestDto(), null);
        when(userRepository.findByEmail("empleado@existente.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authenticationService.registerEmployee(employeeDto));
        assertEquals("Ya existe un usuario registrado con este email", exception.getMessage());
    }

    @Test
    void testRegisterInspectorSuccess() {
        InspectorRequestDto inspectorDto = new InspectorRequestDto("inspector@test.com", "Inspector Gadget", "600333444","12345678J");
        when(userRepository.findByEmail("inspector@test.com")).thenReturn(Optional.empty());
        when(userService.getAuthenticatedUser()).thenReturn(adminUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPasswordForInspector");

        authenticationService.registerInspector(inspectorDto);

        verify(userRepository).saveAndFlush(any(User.class));
        verify(profileRepository).saveAndFlush(any(Profile.class));
        verify(emailService).sendNewInspectorWelcomeEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessForgotPasswordSuccess() {
        String email = "usuario@olvidadizo.com";
        User user = User.builder().id(UUID.randomUUID()).email(email).company(company).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authenticationService.processForgotPassword(email);

        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(email), anyString(), eq(company.getCompanyName()), eq(company.getLogoUrl()));
    }

    @Test
    void testProcessForgotPasswordUserNotFound() {
        String email = "noexiste@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        authenticationService.processForgotPassword(email);

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testExecutePasswordResetSuccess() {
        String token = "valid-token";
        User user = User.builder().id(UUID.randomUUID()).email("test@test.com").profile(new Profile()).build();
        PasswordResetToken resetToken = new PasswordResetToken(UUID.randomUUID(), token, user, OffsetDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        authenticationService.executePasswordReset(token, "newPassword", "newPassword");

        assertEquals("newHashedPassword", user.getPasswordHash());
        assertFalse(user.getProfile().getIsFirstLogin());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    void testExecutePasswordResetTokenExpired() {
        String token = "expired-token";
        PasswordResetToken resetToken = new PasswordResetToken(UUID.randomUUID(), token, new User(), OffsetDateTime.now().minusHours(1));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authenticationService.executePasswordReset(token, "newPassword", "newPassword"));

        assertEquals("El enlace ha caducado. Solicita uno nuevo.", exception.getMessage());
        verify(passwordResetTokenRepository).delete(resetToken);
        verify(userRepository, never()).save(any());
    }
}
