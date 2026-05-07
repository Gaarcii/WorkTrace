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

import java.util.Map;

/**
 * Controlador para gestionar la autenticación y el registro de usuarios.
 * Proporciona endpoints para el registro de empresas, inicio de sesión,
 * cambio de contraseña y registro de empleados e inspectores.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Registra una nueva empresa en el sistema.
     *
     * @param requestDto Datos de la empresa a registrar.
     * @return Una respuesta con el token de autenticación.
     */
    @PostMapping("/register-company")
    public ResponseEntity<AuthResponseDto> registerCompany(@Valid @RequestBody CompanyRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.registerCompany(requestDto));
    }

    /**
     * Autentica a un usuario y le proporciona un token de acceso.
     * @param requestDto Credenciales del usuario.
     * @return Una respuesta con el token de autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto requestDto) {
        return ResponseEntity.ok(authenticationService.signIn(requestDto));
    }

    /**
     * Permite a un usuario autenticado cambiar su contraseña.
     * @param requestDto La nueva contraseña y la confirmación.
     * @return Una respuesta indicando que la contraseña se ha actualizado.
     */
    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        authenticationService.changePassword(requestDto);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    /**
     * Registra un nuevo empleado en la empresa del administrador que realiza la petición.
     * @param requestDto Datos del empleado a registrar.
     * @return Una respuesta indicando que el empleado se ha creado.
     */
    @PostMapping("/register-employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        authenticationService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Empleado creado correctamente"));
    }

    /**
     * Registra un nuevo inspector. Si el inspector ya existe, se le envía un correo para reestablecer la contraseña.
     * @param requestDto Datos del inspector a registrar.
     * @return Una respuesta indicando que el inspector se ha procesado.
     */
    @PostMapping("/register-inspector")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerInspector(@Valid @RequestBody InspectorRequestDto requestDto) {
        authenticationService.registerInspector(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Inspector procesado correctamente. Si era nuevo se ha creado y enviado el correo. Si ya existía, se ha enviado el correo para reestablecer la contraseña."));
    }

    /**
     * Inicia el proceso de recuperación de contraseña para un usuario.
     * @param request El correo electrónico del usuario.
     * @return Una respuesta genérica para no revelar si el correo existe en el sistema.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."));
    }

    /**
     * Permite a un usuario reestablecer su contraseña utilizando un token de seguridad.
     * @param request El token de seguridad y la nueva contraseña.
     * @return Una respuesta indicando que la contraseña se ha actualizado.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.executePasswordReset(
                request.getToken(),
                request.getNewPassword(),
                request.getRepeatPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }
}
