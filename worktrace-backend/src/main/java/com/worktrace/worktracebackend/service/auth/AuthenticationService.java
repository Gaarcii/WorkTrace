package com.worktrace.worktracebackend.service.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.exception.InvalidCredentialsException;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.security.JwtService;
import lombok.RequiredArgsConstructor;
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

        return new AuthResponseDto(jwt);
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
        return new AuthResponseDto(jwt);
    }
}
