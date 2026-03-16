package com.worktrace.worktracebackend.controller.auth;

import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.service.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    // POST http://localhost:8080/api/auth/register-company
    @PostMapping("/register-company")
    public ResponseEntity<AuthResponseDto> registerCompany(@Valid @RequestBody CompanyRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.registerCompany(requestDto));
    }

    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.signIn(requestDto));
    }
}