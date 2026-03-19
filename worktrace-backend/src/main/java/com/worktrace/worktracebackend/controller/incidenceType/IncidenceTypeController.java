package com.worktrace.worktracebackend.controller.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.service.incidenceType.IncidenceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
