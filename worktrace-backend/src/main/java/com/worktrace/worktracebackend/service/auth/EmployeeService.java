package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.service.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void registerEmployee(EmployeeRequestDto requestDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No se ha encontrado el administrador (no autenticado)");
        }

        String adminEmail = authentication.getName();

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new NotFoundException("No se ha encontrado el administrador con email: " + adminEmail));
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
