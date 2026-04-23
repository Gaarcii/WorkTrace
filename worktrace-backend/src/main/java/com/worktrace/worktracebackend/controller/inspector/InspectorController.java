package com.worktrace.worktracebackend.controller.inspector;

import com.worktrace.worktracebackend.dto.inspector.EmpleadoDetalleDto;
import com.worktrace.worktracebackend.dto.inspector.EmpleadoDto;
import com.worktrace.worktracebackend.dto.inspector.InspectorHomeResponseDto;
import com.worktrace.worktracebackend.dto.inspector.InspectorIncidenceDto;
import com.worktrace.worktracebackend.service.inspector.InspectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/inspector")
@RequiredArgsConstructor
public class InspectorController {

    private final InspectorService inspectorService;

    @GetMapping("/home")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<InspectorHomeResponseDto> getHome() {
        return ResponseEntity.ok(inspectorService.getHome());
    }

    @GetMapping("/empleados")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<EmpleadoDto>> getEmpleados(
            Pageable pageable, 
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inspectorService.getEmpleados(pageable, search));
    }

    @GetMapping("/empleados/{id}")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<EmpleadoDetalleDto> getEmpleadoDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(inspectorService.getEmpleadoDetalle(id));
    }

    @GetMapping("/incidencias")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorIncidenceDto>> getIncidencias(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID tipoIncidenciaId,
            @RequestParam(required = false) String busqueda,
            Pageable pageable) {
        
        return ResponseEntity.ok(
                inspectorService.getIncidenciasFiltradas(estado, tipoIncidenciaId, busqueda, pageable)
        );
    }
}
