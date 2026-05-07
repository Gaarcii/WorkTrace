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

/**
 * Controlador para gestionar las incidencias de los empleados.
 * Permite a los empleados crear y ver sus incidencias, y a los administradores
 * gestionarlas.
 */
@RestController
@RequestMapping("/api/incidences")
@RequiredArgsConstructor
public class IncidenceController {

    private final IncidenceService incidenceService;

    /**
     * Obtiene todas las incidencias del usuario autenticado.
     *
     * @return Una lista de las incidencias del usuario.
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkerIncidenceResponseDto>> getIncidencesByUserId() {
        List<WorkerIncidenceResponseDto> responseDtoList =
                incidenceService.getIncidencesByUserId();
        return ResponseEntity.ok(responseDtoList);
    }

    /**
     * Permite a un empleado crear una nueva incidencia.
     * @param requestDto Los datos de la incidencia a crear.
     * @return La incidencia creada.
     */
    @PostMapping()
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<WorkerIncidenceResponseDto> createIncidence(
            @Valid @RequestBody WorkerIncidenceRequestDto requestDto) {
        WorkerIncidenceResponseDto responseDto = incidenceService.createIncidence(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene las incidencias de la empresa por estado.
     * Si no se especifica un estado, por defecto se obtienen las pendientes.
     * @param status El estado de las incidencias a obtener.
     * @param pageable La información de paginación.
     * @return Una página de incidencias.
     */
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

    /**
     * Obtiene el historial de incidencias de la empresa.
     * @param pageable La información de paginación.
     * @return Una página con el historial de incidencias.
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminIncidenceResponseDto>> getCompanyIncidenceHistory(Pageable pageable) {
        Page<AdminIncidenceResponseDto> responseDtos =
                incidenceService.getCompanyIncidenceHistory(pageable);

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Permite a un administrador gestionar una incidencia (aprobarla o rechazarla).
     * @param incidenceId El ID de la incidencia a gestionar.
     * @param dto Los datos para la gestión de la incidencia.
     * @return Una respuesta vacía si la operación fue exitosa.
     */
    @PatchMapping("/{id}/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> manageIncidence(
            @PathVariable("id") UUID incidenceId,
            @Valid @RequestBody AdminIncidenceRequestDto dto) {

        incidenceService.manageIncidence(incidenceId, dto);
        return ResponseEntity.ok().build();
    }

}
