package com.worktrace.worktracebackend.controller.jobPosition;

import com.worktrace.worktracebackend.dto.jobPosition.JobPositionRequestDto;
import com.worktrace.worktracebackend.dto.jobPosition.JobPositionResponseDto;
import com.worktrace.worktracebackend.service.jobPosition.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobPosition")
@RequiredArgsConstructor
public class JobPositionController {
    private final JobPositionService jobPositionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobPositionResponseDto>> getPuestos() {
        List<JobPositionResponseDto> responseDtos = jobPositionService.getPuestosTrabajo();
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPositionResponseDto> postPuestos(
            @RequestBody JobPositionRequestDto requestDto) {
        JobPositionResponseDto responseDto = jobPositionService.crearNuevoPuesto(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePuestos(@PathVariable("id") UUID puestoID) {
        jobPositionService.eliminarPuesto(puestoID);
        return ResponseEntity.ok().build();
    }
}
