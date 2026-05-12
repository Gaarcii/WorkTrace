package com.worktrace.worktracebackend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dto.auth.AuthRequestDto;
import com.worktrace.worktracebackend.dto.auth.AuthResponseDto;
import com.worktrace.worktracebackend.dto.company.CompanyRequestDto;
import com.worktrace.worktracebackend.dto.passwordResetToken.ForgotPasswordRequest;
import com.worktrace.worktracebackend.dto.passwordResetToken.ResetPasswordRequest;
import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.dto.user.InspectorRequestDto;
import com.worktrace.worktracebackend.dto.user.PasswordChangeRequestDto;
import com.worktrace.worktracebackend.service.auth.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
        companyRequestDto = new CompanyRequestDto("Test Company", "test@company.com", "Admin", "User", "password123");
        authRequestDto = new AuthRequestDto("test@user.com", "password123");
        authResponseDto = new AuthResponseDto("jwt-token-12345");
        passwordChangeRequestDto = new PasswordChangeRequestDto("newPassword", "newPassword");
        employeeRequestDto = new EmployeeRequestDto("Employee", "User", "employee@test.com", "password123");
        inspectorRequestDto = new InspectorRequestDto("Inspector", "User", "inspector@test.com");
        forgotPasswordRequest = new ForgotPasswordRequest("user@example.com");
        resetPasswordRequest = new ResetPasswordRequest("reset-token", "newPassword123", "newPassword123");
    }

    @Test
    void testRegisterCompany_Success() throws Exception {
        when(authenticationService.registerCompany(any(CompanyRequestDto.class))).thenReturn(authResponseDto);

        mockMvc.perform(post("/api/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-12345"));
    }

    @Test
    void testRegisterCompany_InvalidData() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto("", "", "", "", "");

        mockMvc.perform(post("/api/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authenticationService.signIn(any(AuthRequestDto.class))).thenReturn(authResponseDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-12345"));
    }

    @Test
    @WithMockUser
    void testUpdatePassword_Success() throws Exception {
        doNothing().when(authenticationService).changePassword(any(PasswordChangeRequestDto.class));

        mockMvc.perform(patch("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterEmployee_Success() throws Exception {
        doNothing().when(authenticationService).registerEmployee(any(EmployeeRequestDto.class));

        mockMvc.perform(post("/api/auth/register-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Empleado creado correctamente"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testRegisterEmployee_Forbidden() throws Exception {
        mockMvc.perform(post("/api/auth/register-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterInspector_Success() throws Exception {
        doNothing().when(authenticationService).registerInspector(any(InspectorRequestDto.class));

        mockMvc.perform(post("/api/auth/register-inspector")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspectorRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Inspector procesado correctamente. Si era nuevo se ha creado y enviado el correo. Si ya existía, se ha enviado el correo para reestablecer la contraseña."));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        doNothing().when(authenticationService).processForgotPassword(any(String.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        doNothing().when(authenticationService).executePasswordReset(any(String.class), any(String.class), any(String.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente."));
    }
}
