package com.worktrace.worktracebackend.controller.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeItemDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeRequestDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.service.incidenceType.IncidenceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidence/types")
@RequiredArgsConstructor
public class IncidenceTypeController {

    private final IncidenceTypeService incidenceTypeService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidenceTypeResponseDto> tiposIncidencias() {
        IncidenceTypeResponseDto responseDto = incidenceTypeService
                .getTiposIncidencias();
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidenceTypeItemDto> createIncidenceType(
            @Valid @RequestBody IncidenceTypeRequestDto dto) {
        IncidenceTypeItemDto created = incidenceTypeService.createIncidenceType(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidenceTypeItemDto> updateIncidenceType(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenceTypeRequestDto dto) {
        IncidenceTypeItemDto updated = incidenceTypeService.updateIncidenceType(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIncidenceType(@PathVariable UUID id) {
        incidenceTypeService.deleteIncidenceType(id);
        return ResponseEntity.noContent().build();
    }

}
