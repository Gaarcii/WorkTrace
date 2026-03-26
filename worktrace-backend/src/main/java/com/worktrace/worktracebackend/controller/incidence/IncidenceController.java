package com.worktrace.worktracebackend.controller.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.EstadoIncidencia;
import com.worktrace.worktracebackend.service.incidence.IncidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidence")
@RequiredArgsConstructor
public class IncidenceController {

    private final IncidenceService incidenceService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkerIncidenceResponseDto>> getIncidenceByUserId() {
        List<WorkerIncidenceResponseDto> responseDtoList =
                incidenceService.getIncidenciasByUserID();
        return ResponseEntity.ok(responseDtoList);
    }

    @PostMapping()
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<WorkerIncidenceResponseDto> postIncidencia(
            @Valid @RequestBody WorkerIncidenceRequestDto requestDto) {
        WorkerIncidenceResponseDto responseDto = incidenceService.postIncidence(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminIncidenceResponseDto>> getCompanyIncidences(
            @RequestParam(required = false) EstadoIncidencia status,
            Pageable pageable) {

        if (status == null) {
            status = EstadoIncidencia.PENDING;
        }
        Page<AdminIncidenceResponseDto> responsePage = incidenceService
                .getIncidenciasByCompanyAndStatus(status, pageable);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminIncidenceResponseDto>> getHistoryIncindence(Pageable pageable) {
        Page<AdminIncidenceResponseDto> responseDtos =
                incidenceService.getIncidenciasByCompanyStatusIn(pageable);

        return ResponseEntity.ok(responseDtos);
    }

    @PatchMapping("/{id}/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> gestionarIncidencia(
            @PathVariable("id") UUID incidenceId,
            @Valid @RequestBody AdminIncidenceRequestDto dto) {

        incidenceService.gestionarIncidencia(incidenceId, dto);
        return ResponseEntity.ok().build();
    }

}
