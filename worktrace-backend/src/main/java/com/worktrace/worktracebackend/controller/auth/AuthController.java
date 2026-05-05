package com.worktrace.worktracebackend.controller.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.passwordResetToken.ForgotPasswordRequest;
import com.worktrace.worktracebackend.dto.passwordResetToken.ResetPasswordRequest;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.InspectorRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.service.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

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
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        try {
            authenticationService.changePassword(requestDto);
            return ResponseEntity.ok(Collections
                    .singletonMap("message", "Contraseña actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/register-employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        authenticationService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Empleado creado correctamente"));
    }

    @PostMapping("/register-inspector")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerInspector(@Valid @RequestBody InspectorRequestDto requestDto) {
        authenticationService.registerInspector(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Inspector procesado correctamente. Si era nuevo se ha creado y enviado el correo. Si ya existía, se ha enviado el correo para reestablecer la contraseña."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.executePasswordReset(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getRepeatPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
