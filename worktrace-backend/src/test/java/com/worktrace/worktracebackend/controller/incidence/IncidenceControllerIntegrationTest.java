package com.worktrace.worktracebackend.controller.incidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.IncidenceStatus;
import com.worktrace.worktracebackend.service.incidence.IncidenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
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
class IncidenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidenceService incidenceService;

    private WorkerIncidenceRequestDto workerIncidenceRequestDto;
    private WorkerIncidenceResponseDto workerIncidenceResponseDto;
    private AdminIncidenceRequestDto adminIncidenceRequestDto;
    private AdminIncidenceResponseDto adminIncidenceResponseDto;
    private UUID incidenceId;

    @BeforeEach
    void setUp() {
        incidenceId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        workerIncidenceRequestDto = new WorkerIncidenceRequestDto(
                typeId,
                LocalDate.now(),
                LocalTime.of(9, 0),
                "Comentario de prueba"
        );

        workerIncidenceResponseDto = new WorkerIncidenceResponseDto(
                "Tipo de Incidencia",
                LocalDate.now(),
                LocalTime.of(9, 0),
                "Comentario de prueba",
                IncidenceStatus.PENDING,
                OffsetDateTime.now()
        );

        adminIncidenceRequestDto = new AdminIncidenceRequestDto(
                IncidenceStatus.RESOLVED,
                "Incidencia resuelta"
        );

        adminIncidenceResponseDto = new AdminIncidenceResponseDto(
                incidenceId,
                "John Doe",
                "Developer",
                "Tipo de Incidencia",
                "Comentario de prueba",
                IncidenceStatus.PENDING,
                LocalDate.now(),
                OffsetDateTime.now(),
                "http://example.com/avatar.png",
                null
        );
    }

    @Test
    @WithMockUser
    void getIncidencesByUserId_shouldReturnUserIncidences() throws Exception {
        when(incidenceService.getIncidencesByUserId()).thenReturn(Collections.singletonList(workerIncidenceResponseDto));

        mockMvc.perform(get("/api/incidences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].incidenceType").value(workerIncidenceResponseDto.getIncidenceType()))
                .andExpect(jsonPath("$[0].status").value(workerIncidenceResponseDto.getStatus().toString()));
    }

    @Test
    void getIncidencesByUserId_shouldReturnForbiddenWithoutUser() throws Exception {
        mockMvc.perform(get("/api/incidences"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void createIncidence_shouldCreateIncidenceForWorker() throws Exception {
        when(incidenceService.createIncidence(any(WorkerIncidenceRequestDto.class))).thenReturn(workerIncidenceResponseDto);

        mockMvc.perform(post("/api/incidences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerIncidenceRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidenceType").value(workerIncidenceResponseDto.getIncidenceType()))
                .andExpect(jsonPath("$.comment").value(workerIncidenceResponseDto.getComment()));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void createIncidence_whenInvalidData_shouldReturnBadRequest() throws Exception {
        WorkerIncidenceRequestDto invalidDto = new WorkerIncidenceRequestDto(null, null, null, null);

        mockMvc.perform(post("/api/incidences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createIncidence_whenUserIsAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/incidences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerIncidenceRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompanyIncidencesByStatus_shouldReturnPendingIncidences() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminIncidenceResponseDto> page = new PageImpl<>(Collections.singletonList(adminIncidenceResponseDto), pageable, 1);
        when(incidenceService.getCompanyIncidencesByStatus(IncidenceStatus.PENDING, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/incidences/admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(adminIncidenceResponseDto.getId().toString()))
                .andExpect(jsonPath("$.content[0].status").value(IncidenceStatus.PENDING.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompanyIncidencesByStatus_withStatusParam_shouldReturnIncidences() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminIncidenceResponseDto> page = new PageImpl<>(Collections.singletonList(adminIncidenceResponseDto), pageable, 1);
        when(incidenceService.getCompanyIncidencesByStatus(IncidenceStatus.RESOLVED, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/incidences/admin")
                        .param("status", "RESOLVED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void getCompanyIncidencesByStatus_whenUserIsWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/incidences/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompanyIncidenceHistory_shouldReturnHistory() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        List<AdminIncidenceResponseDto> historyList = Collections.singletonList(adminIncidenceResponseDto);
        Page<AdminIncidenceResponseDto> responsePage = new PageImpl<>(historyList, pageable, 1);
        when(incidenceService.getCompanyIncidenceHistory(pageable)).thenReturn(responsePage);

        mockMvc.perform(get("/api/incidences/history")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(adminIncidenceResponseDto.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void getCompanyIncidenceHistory_whenUserIsWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/incidences/history"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageIncidence_shouldUpdateIncidenceStatus() throws Exception {
        doNothing().when(incidenceService).manageIncidence(any(UUID.class), any(AdminIncidenceRequestDto.class));

        mockMvc.perform(patch("/api/incidences/{id}/manage", incidenceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminIncidenceRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageIncidence_whenInvalidData_shouldReturnBadRequest() throws Exception {
        AdminIncidenceRequestDto invalidDto = new AdminIncidenceRequestDto(null, null);

        mockMvc.perform(patch("/api/incidences/{id}/manage", incidenceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void manageIncidence_whenUserIsWorker_shouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/api/incidences/{id}/manage", incidenceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminIncidenceRequestDto)))
                .andExpect(status().isForbidden());
    }
}

