package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.exception.InvalidCredentialsException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.security.JwtService;
import com.worktrace.worktracebackend.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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

    @Transactional
    public void cambiarContrasena(PasswordChangeRequestDto requestDto) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        User user = info.getUser();

        boolean comprobarActual = passwordEncoder.matches(requestDto.getActual(), user.getPasswordHash());
        boolean comprobarNueva = requestDto.getNueva().equals(requestDto.getRepetir());

        if (!comprobarActual || !comprobarNueva) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta o las nuevas no coinciden.");
        }

        user.setPasswordHash(passwordEncoder.encode(requestDto.getNueva()));

        user.getProfile().setIsFirstLogin(false);
    }

    @jakarta.transaction.Transactional
    public void registerEmployee(EmployeeRequestDto requestDto) {
        User admin = userService.getAuthenticatedUser();
        Company company = admin.getCompany();

        String password = generarPasswordSegura();

        User employee = User.builder()
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.WORKER)
                .isEnabled(true)
                .createdAt(OffsetDateTime.now())
                .company(company)
                .build();
        userRepository.save(employee);

        Profile profile = Profile.builder()
                .fullName(requestDto.getProfile().getFullName())
                .employeeCode(requestDto.getProfile().getEmployeeCode())
                .phone(requestDto.getProfile().getPhone())
                .isFirstLogin(true)
                .isActive(true)
                .updatedAt(OffsetDateTime.now())
                .user(employee)
                .build();
        profileRepository.save(profile);

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

    private String generarPasswordSegura() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit).get();

        return generator.generate(10);
    }
}
