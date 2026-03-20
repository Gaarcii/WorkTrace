package com.worktrace.worktracebackend.controller.incidence;

import com.worktrace.worktracebackend.dto.incidence.IncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.IncidenceResponseDto;
import com.worktrace.worktracebackend.service.incidence.IncidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidence")
@RequiredArgsConstructor
public class IncidenceController {

    private final IncidenceService incidenceService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncidenceResponseDto>> getIncidenceByUserId() {
        List<IncidenceResponseDto> responseDtoList =
                incidenceService.getIncidenciasByUserID();
        return ResponseEntity.ok(responseDtoList);
    }

    @PostMapping()
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<IncidenceResponseDto> postIncidencia(
            @Valid @RequestBody IncidenceRequestDto requestDto) {
        IncidenceResponseDto responseDto = incidenceService.postIncidence(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
