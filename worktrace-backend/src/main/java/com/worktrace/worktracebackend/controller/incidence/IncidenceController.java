package com.worktrace.worktracebackend.controller.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.IncidenceStatus;
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
@RequestMapping("/api/incidences")
@RequiredArgsConstructor
public class IncidenceController {

    private final IncidenceService incidenceService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkerIncidenceResponseDto>> getIncidencesByUserId() {
        List<WorkerIncidenceResponseDto> responseDtoList =
                incidenceService.getIncidencesByUserId();
        return ResponseEntity.ok(responseDtoList);
    }

    @PostMapping()
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<WorkerIncidenceResponseDto> createIncidence(
            @Valid @RequestBody WorkerIncidenceRequestDto requestDto) {
        WorkerIncidenceResponseDto responseDto = incidenceService.createIncidence(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminIncidenceResponseDto>> getCompanyIncidencesByStatus(
            @RequestParam(required = false) IncidenceStatus status,
            Pageable pageable) {

        if (status == null) {
            status = IncidenceStatus.PENDING;
        }
        Page<AdminIncidenceResponseDto> responsePage = incidenceService
                .getCompanyIncidencesByStatus(status, pageable);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminIncidenceResponseDto>> getCompanyIncidenceHistory(Pageable pageable) {
        Page<AdminIncidenceResponseDto> responseDtos =
                incidenceService.getCompanyIncidenceHistory(pageable);

        return ResponseEntity.ok(responseDtos);
    }

    @PatchMapping("/{id}/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> manageIncidence(
            @PathVariable("id") UUID incidenceId,
            @Valid @RequestBody AdminIncidenceRequestDto dto) {

        incidenceService.manageIncidence(incidenceId, dto);
        return ResponseEntity.ok().build();
    }

}