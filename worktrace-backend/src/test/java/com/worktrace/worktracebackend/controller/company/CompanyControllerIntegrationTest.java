package com.worktrace.worktracebackend.controller.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dto.company.CompanyResponseDto;
import com.worktrace.worktracebackend.dto.company.UpdateCompanyDto;
import com.worktrace.worktracebackend.service.company.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;

    private CompanyResponseDto companyResponseDto;
    private UpdateCompanyDto updateCompanyDto;

    @BeforeEach
    void setUp() {
        companyResponseDto = new CompanyResponseDto(
                "Test Company",
                "B12345678",
                "http://example.com/logo.png"
        );

        updateCompanyDto = new UpdateCompanyDto(
                "New Test Company",
                "A87654321"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyCompanyData_shouldReturnCompanyData() throws Exception {
        when(companyService.getMyCompanyData()).thenReturn(companyResponseDto);

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value(companyResponseDto.getCompanyName()))
                .andExpect(jsonPath("$.cif").value(companyResponseDto.getCif()))
                .andExpect(jsonPath("$.logoUrl").value(companyResponseDto.getLogoUrl()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyCompanyData_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyCompanyData_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMyCompanyLogo_shouldUpdateLogo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image".getBytes()
        );

        String newLogoUrl = "http://example.com/new-logo.png";
        when(companyService.updateMyCompanyLogo(any(MockMultipartFile.class))).thenReturn(newLogoUrl);

        mockMvc.perform(multipart("/api/companies/logo")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logo de la empresa actualizado correctamente"))
                .andExpect(jsonPath("$.logoUrl").value(newLogoUrl));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCompanyData_shouldUpdateData() throws Exception {
        doNothing().when(companyService).updateMyCompanyData(any(UpdateCompanyDto.class));

        mockMvc.perform(patch("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Datos de la empresa actualizados correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCompanyData_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        UpdateCompanyDto invalidDto = new UpdateCompanyDto("", "123");

        mockMvc.perform(patch("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCompanyData_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCompanyData_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDto)))
                .andExpect(status().isForbidden());
    }
}
