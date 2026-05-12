package com.worktrace.worktracebackend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.passwordResetToken.ForgotPasswordRequest;
import com.worktrace.worktracebackend.dto.passwordResetToken.ResetPasswordRequest;
import com.worktrace.worktracebackend.dto.user.*;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.service.auth.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private CompanyRequestDto companyRequestDto;
    private AuthRequestDto authRequestDto;
    private AuthResponseDto authResponseDto;
    private PasswordChangeRequestDto passwordChangeRequestDto;
    private EmployeeRequestDto employeeRequestDto;
    private InspectorRequestDto inspectorRequestDto;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        AdminProfileRequestDto adminProfile = new AdminProfileRequestDto(
                "Administrador Ejemplo",
                "12345678A",
                "600111222"
        );
        AdminRequestDto adminRequest = new AdminRequestDto(
                "admin@empresa.test",
                "contraseñaSegura",
                adminProfile
        );
        companyRequestDto = new CompanyRequestDto(
                "Empresa Test S.L.",
                "B12345678",
                adminRequest
        );

        authRequestDto = new AuthRequestDto("usuario@test.com", "miContraseña");

        authResponseDto = new AuthResponseDto("token-de-prueba", true, Role.ADMIN);

        passwordChangeRequestDto = new PasswordChangeRequestDto("actualPass", "nuevaPass", "nuevaPass");

        ProfileRequestDto employeeProfile = new ProfileRequestDto(
                "Empleado Ejemplo",
                "87654321B",
                "600222333",
                java.util.UUID.randomUUID(),
                new java.math.BigDecimal("40")
        );
        employeeRequestDto = new EmployeeRequestDto("empleado@test.com", employeeProfile, null);

        inspectorRequestDto = new InspectorRequestDto("inspector@test.com", "Inspector Nombre", "600333444");

        forgotPasswordRequest = new ForgotPasswordRequest("usuario@existente.test");

        resetPasswordRequest = new ResetPasswordRequest("token-abc", "nuevaPass", "nuevaPass");
    }

    @Test
    void testRegisterCompanySuccess() throws Exception {
        when(authenticationService.registerCompany(any(CompanyRequestDto.class))).thenReturn(authResponseDto);

        mockMvc.perform(post("/api/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.firstLogin").value(true))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(authenticationService.signIn(any(AuthRequestDto.class))).thenReturn(authResponseDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }

    @Test
    @WithMockUser
    void testUpdatePasswordSuccess() throws Exception {
        doNothing().when(authenticationService).changePassword(any(PasswordChangeRequestDto.class));

        mockMvc.perform(patch("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterEmployeeCreated() throws Exception {
        doNothing().when(authenticationService).registerEmployee(any(EmployeeRequestDto.class));

        mockMvc.perform(post("/api/auth/register-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Empleado creado correctamente"));
    }

    @Test
    void testRegisterEmployeeUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/register-employee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterInspectorCreated() throws Exception {
        doNothing().when(authenticationService).registerInspector(any(InspectorRequestDto.class));

        mockMvc.perform(post("/api/auth/register-inspector")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspectorRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Inspector procesado correctamente. Si era nuevo se ha creado y enviado el correo. Si ya existía, se ha enviado el correo para reestablecer la contraseña."));
    }

    @Test
    void testForgotPasswordGenericResponse() throws Exception {
        doNothing().when(authenticationService).processForgotPassword(any(String.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."));
    }

    @Test
    void testResetPasswordSuccess() throws Exception {
        doNothing().when(authenticationService).executePasswordReset(any(String.class), any(String.class), any(String.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente."));
    }

    @Test
    void testResetPasswordNotFound() throws Exception {
        doThrow(new NotFoundException("Token no encontrado")).when(authenticationService)
                .executePasswordReset(any(String.class), any(String.class), any(String.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Token no encontrado"));
    }

    @Test
    void testLoginValidationErrorInvalidEmail() throws Exception {
        AuthRequestDto invalid = new AuthRequestDto("no-es-un-email", "abc123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Formato no válido, debe ser un email"));
    }
}

