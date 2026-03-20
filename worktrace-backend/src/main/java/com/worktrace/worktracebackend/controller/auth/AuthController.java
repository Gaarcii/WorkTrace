package com.worktrace.worktracebackend.controller.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.service.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register-company")
    public ResponseEntity<AuthResponseDto> registerCompany(@Valid @RequestBody CompanyRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.registerCompany(requestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.signIn(requestDto));
    }

    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarContrasena(
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        try {
            authenticationService.cambiarContrasena(requestDto);
            return ResponseEntity.ok(Collections
                    .singletonMap("message", "Contraseña actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/register-employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        authenticationService.registerEmployee(requestDto);
        return new ResponseEntity<>("Empleado creado correctamente", HttpStatus.CREATED);
    }
}