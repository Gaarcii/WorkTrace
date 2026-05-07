package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.InspectorRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.exception.InvalidCredentialsException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.*;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.email.EmailService;
import com.worktrace.worktracebackend.service.workSchedule.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestionar la autenticación, el registro de usuarios y la gestión de contraseñas.
 * Centraliza la lógica de negocio relacionada con el acceso y la identidad de los usuarios en el sistema.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;
    private final JobPositionRepository jobPositionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final WorkScheduleService workScheduleService;


    /**
     * Registra una nueva empresa junto con su usuario administrador.
     * Esta operación es el punto de entrada para nuevas empresas en la plataforma.
     * @param requestDto Datos de la empresa y del administrador a registrar.
     * @return Un DTO con el token de autenticación para el nuevo administrador.
     */
    @Transactional
    public AuthResponseDto registerCompany(CompanyRequestDto requestDto) {
        Company company = Company.builder()
                .companyName(requestDto.getCompanyName())
                .cif(requestDto.getCif())
                .updatedAt(OffsetDateTime.now())
                .build();
        companyRepository.save(company);

        User admin = User.builder()
                .email(requestDto.getAdmin().getEmail())
                .passwordHash(passwordEncoder.encode(requestDto.getAdmin().getPassword()))
                .role(Role.ADMIN)
                .isEnabled(true)
                .createdAt(OffsetDateTime.now())
                .company(company)
                .build();
        userRepository.save(admin);

        Profile profile = Profile.builder()
                .fullName(requestDto.getAdmin().getProfile().getFullName())
                .employeeCode(requestDto.getAdmin().getProfile().getEmployeeCode())
                .phone(requestDto.getAdmin().getProfile().getPhone())
                .isFirstLogin(false)
                .isActive(true)
                .updatedAt(OffsetDateTime.now())
                .user(admin)
                .build();
        profileRepository.save(profile);

        String jwt = jwtService.generateToken(admin);

        Boolean firstLogin = profile.getIsFirstLogin();
        Role role = admin.getRole();
        return new AuthResponseDto(jwt, firstLogin, role);
    }

    /**
     * Autentica a un usuario en el sistema.
     * Valida las credenciales y, si son correctas, genera y devuelve un token JWT.
     * @param requestDto Credenciales del usuario (email y contraseña).
     * @return Un DTO con el token de autenticación y datos básicos del usuario.
     */
    public AuthResponseDto signIn(AuthRequestDto requestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Email o contraseña inválidos");
        }
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email o contraseña inválidos"));

        String jwt = jwtService.generateToken(user);
        Boolean firstLogin = user.getProfile().getIsFirstLogin();
        Role role = user.getRole();
        return new AuthResponseDto(jwt, firstLogin, role);
    }

    /**
     * Permite a un usuario autenticado cambiar su propia contraseña.
     * Verifica la contraseña actual antes de permitir el cambio.
     * @param requestDto Contiene la contraseña actual y la nueva contraseña con su confirmación.
     */
    @Transactional
    public void changePassword(PasswordChangeRequestDto requestDto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        User user = info.getUser();

        boolean isCurrentPasswordCorrect = passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPasswordHash());
        boolean doNewPasswordsMatch = requestDto.getNewPassword().equals(requestDto.getRepeatPassword());

        if (!isCurrentPasswordCorrect || !doNewPasswordsMatch) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta o las nuevas no coinciden.");
        }

        user.setPasswordHash(passwordEncoder.encode(requestDto.getNewPassword()));

        user.getProfile().setIsFirstLogin(false);
    }

    /**
     * Registra un nuevo empleado en la empresa del administrador autenticado.
     * Crea el usuario, el perfil, asigna horarios si se proporcionan y envía un correo de bienvenida.
     * @param requestDto Datos del nuevo empleado, incluyendo perfil y horarios opcionales.
     */
    @Transactional
    public void registerEmployee(EmployeeRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este email");
        }

        User admin = userService.getAuthenticatedUser();
        Company company = admin.getCompany();

        JobPosition puesto = jobPositionRepository.findById(
                requestDto.getProfile().getPositionId()).orElseThrow(() ->
                new IllegalArgumentException("El puesto de trabajo no existe"));

        if (!puesto.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("El puesto no pertenece a tu empresa");
        }

        String password = generarPasswordSegura();

        User employee = User.builder()
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.WORKER)
                .isEnabled(true)
                .createdAt(OffsetDateTime.now())
                .company(company)
                .build();
        userRepository.saveAndFlush(employee);

        Profile profile = Profile.builder()
                .fullName(requestDto.getProfile().getFullName())
                .employeeCode(requestDto.getProfile().getEmployeeCode())
                .phone(requestDto.getProfile().getPhone())
                .position(puesto)
                .weeklyHours(requestDto.getProfile().getWeeklyHours())
                .isFirstLogin(true)
                .isActive(true)
                .updatedAt(OffsetDateTime.now())
                .user(employee)
                .build();
        profileRepository.saveAndFlush(profile);

        if (requestDto.getSchedules() != null && !requestDto.getSchedules().isEmpty()) {
            WorkScheduleRequestDto scheduleDto = new WorkScheduleRequestDto();
            scheduleDto.setEmployeeId(employee.getId());
            scheduleDto.setSchedules(requestDto.getSchedules());
            workScheduleService.assignWorkSchedule(scheduleDto);
        }

        emailService.sendNewEmployeeWelcomeEmail(
                requestDto.getEmail(),
                requestDto.getProfile().getFullName(),
                password,
                company.getLogoUrl(),
                company.getCompanyName(),
                admin.getProfile().getFullName(),
                "https://app.worktrace.com/login" //CAMBIAR A URL DEL DOMINIO
        );
    }

    /**
     * Registra un nuevo inspector. Si el correo ya existe y pertenece a un inspector,
     * inicia el proceso de recuperación de contraseña. Si no existe, crea el nuevo usuario
     * y le envía un correo de bienvenida.
     * @param requestDto Datos del inspector a registrar.
     */
    @Transactional
    public void registerInspector(InspectorRequestDto requestDto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(requestDto.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getRole() != Role.INSPECTOR) {
                throw new IllegalArgumentException("Ya existe un usuario con este email y no es inspector.");
            }
            processForgotPassword(requestDto.getEmail());
            return;
        }

        User admin = userService.getAuthenticatedUser();
        Company company = admin.getCompany();

        String password = generarPasswordSegura();

        User inspector = User.builder()
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.INSPECTOR)
                .isEnabled(true)
                .createdAt(OffsetDateTime.now())
                .company(company)
                .build();
        userRepository.saveAndFlush(inspector);

        Profile profile = Profile.builder()
                .fullName(requestDto.getFullName())
                .employeeCode("N/A")
                .phone(requestDto.getPhone())
                .isFirstLogin(false)
                .isActive(true)
                .updatedAt(OffsetDateTime.now())
                .user(inspector)
                .build();
        profileRepository.saveAndFlush(profile);

        emailService.sendNewInspectorWelcomeEmail(
                requestDto.getEmail(),
                requestDto.getFullName(),
                password,
                company.getLogoUrl(),
                company.getCompanyName(),
                admin.getProfile().getFullName(),
                "https://app.worktrace.com/login" //CAMBIAR A URL DEL DOMINIO
        );
    }

    /**
     * Inicia el proceso de recuperación de contraseña para un usuario.
     * Genera un token de reseteo y envía un correo electrónico con el enlace de recuperación.
     * No revela si el correo electrónico existe o no en el sistema para evitar enumeración de usuarios.
     * @param email El correo electrónico del usuario que ha olvidado su contraseña.
     */
    @Transactional
    public void processForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(OffsetDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:4200/reset-password?token=" + token; // Cambiar en producción
        Company company = user.getCompany();
        String companyName = company != null ? company.getCompanyName() : "WorkTrace";
        String companyLogoUrl = company != null ? company.getLogoUrl() : null;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink, companyName, companyLogoUrl);
    }

    /**
     * Ejecuta el cambio de contraseña utilizando un token de recuperación.
     * Valida el token, comprueba que no haya expirado y actualiza la contraseña del usuario.
     * @param token El token de recuperación de contraseña.
     * @param newPassword La nueva contraseña.
     * @param repeatPassword La confirmación de la nueva contraseña.
     */
    @Transactional
    public void executePasswordReset(String token, String newPassword, String repeatPassword) {
        if (!newPassword.equals(repeatPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("El enlace es inválido o ha caducado."));

        if (resetToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("El enlace ha caducado. Solicita uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.getProfile().setIsFirstLogin(false);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    private String generarPasswordSegura() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit).get();

        return generator.generate(10);
    }
}
