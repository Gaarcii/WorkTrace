package com.worktrace.worktracebackend.controller.incidenceType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeItemDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeProjection;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeRequestDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.service.incidenceType.IncidenceTypeService;
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

import java.util.Collections;
import java.util.UUID;

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
class IncidenceTypeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidenceTypeService incidenceTypeService;

    private IncidenceTypeRequestDto incidenceTypeRequestDto;
    private IncidenceTypeItemDto incidenceTypeItemDto;
    private IncidenceTypeResponseDto incidenceTypeResponseDto;
    private final UUID incidenceTypeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        incidenceTypeRequestDto = new IncidenceTypeRequestDto();
        incidenceTypeRequestDto.setName("Nuevo Tipo de Incidencia");

        incidenceTypeItemDto = new IncidenceTypeItemDto(incidenceTypeId, "Nuevo Tipo de Incidencia");

        IncidenceTypeProjection projection = new IncidenceTypeProjection() {
            @Override
            public UUID getId() {
                return incidenceTypeId;
            }

            @Override
            public String getName() {
                return "Tipo Existente";
            }
        };
        incidenceTypeResponseDto = new IncidenceTypeResponseDto(Collections.singletonList(projection));
    }

    @Test
    @WithMockUser
    void getIncidenceTypes_shouldReturnIncidenceTypes() throws Exception {
        when(incidenceTypeService.getIncidenceTypes()).thenReturn(incidenceTypeResponseDto);

        mockMvc.perform(get("/api/incidence-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types[0].id").value(incidenceTypeId.toString()))
                .andExpect(jsonPath("$.types[0].name").value("Tipo Existente"));
    }

    @Test
    void getIncidenceTypes_withoutUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/incidence-types"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createIncidenceType_asAdmin_shouldCreateIncidenceType() throws Exception {
        when(incidenceTypeService.createIncidenceType(any(IncidenceTypeRequestDto.class))).thenReturn(incidenceTypeItemDto);

        mockMvc.perform(post("/api/incidence-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenceTypeRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(incidenceTypeId.toString()))
                .andExpect(jsonPath("$.name").value("Nuevo Tipo de Incidencia"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void createIncidenceType_asWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/incidence-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenceTypeRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createIncidenceType_withInvalidData_shouldReturnBadRequest() throws Exception {
        IncidenceTypeRequestDto invalidDto = new IncidenceTypeRequestDto();
        invalidDto.setName(""); // Blank name

        mockMvc.perform(post("/api/incidence-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateIncidenceType_asAdmin_shouldUpdateIncidenceType() throws Exception {
        when(incidenceTypeService.updateIncidenceType(any(UUID.class), any(IncidenceTypeRequestDto.class))).thenReturn(incidenceTypeItemDto);

        mockMvc.perform(put("/api/incidence-types/{id}", incidenceTypeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenceTypeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incidenceTypeId.toString()))
                .andExpect(jsonPath("$.name").value("Nuevo Tipo de Incidencia"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void updateIncidenceType_asWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/incidence-types/{id}", incidenceTypeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenceTypeRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteIncidenceType_asAdmin_shouldDeleteIncidenceType() throws Exception {
        doNothing().when(incidenceTypeService).deleteIncidenceType(incidenceTypeId);

        mockMvc.perform(delete("/api/incidence-types/{id}", incidenceTypeId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void deleteIncidenceType_asWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/incidence-types/{id}", incidenceTypeId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
