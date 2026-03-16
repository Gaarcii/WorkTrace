package com.worktrace.worktracebackend.service;

import com.worktrace.worktracebackend.dto.EmployeeRequestDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerEmployee(EmployeeRequestDto requestDto) {
        String adminEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        Company company = admin.getCompany();

        String password = generarPasswordSegura();

        System.out.println("DEBUG: Contraseña generada para " + requestDto.getEmail() + " es: " + password);

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
    }

    private String generarPasswordSegura() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit).get();

        return generator.generate(10);
    }
}
