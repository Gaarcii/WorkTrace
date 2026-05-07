package com.worktrace.worktracebackend.controller.inspector;

import com.worktrace.worktracebackend.dto.inspector.*;
import com.worktrace.worktracebackend.service.inspector.InspectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador para las funcionalidades del rol de Inspector.
 * Proporciona endpoints para que los inspectores puedan consultar información
 * sobre empleados, incidencias, cierres diarios y auditorías del sistema.
 */
@RestController
@RequestMapping("/api/inspector")
@RequiredArgsConstructor
public class InspectorController {

    private final InspectorService inspectorService;

    /**
     * Obtiene los datos principales para la página de inicio del inspector.
     * Esta información suele incluir estadísticas y resúmenes generales.
     *
     * @return Un DTO con los datos para la página de inicio.
     */
    @GetMapping("/home")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<InspectorHomeResponseDto> getHome() {
        return ResponseEntity.ok(inspectorService.getHome());
    }

    /**
     * Obtiene una lista paginada de todos los empleados del sistema.
     * Permite realizar una búsqueda por nombre o NIF.
     * @param pageable Información de paginación.
     * @param search Término de búsqueda opcional.
     * @return Una página de empleados.
     */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inspectorService.getEmployees(pageable, search));
    }

    /**
     * Obtiene los detalles de un empleado específico.
     * @param id El UUID del empleado a consultar.
     * @return Los detalles completos del empleado.
     */
    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<EmployeeDetailDto> getEmployeeDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(inspectorService.getEmployeeDetail(id));
    }

    /**
     * Obtiene una lista filtrada y paginada de incidencias.
     * Permite filtrar por estado, tipo de incidencia y un término de búsqueda general.
     * @param status Estado de la incidencia (PENDING, APPROVED, REJECTED).
     * @param incidenceTypeId UUID del tipo de incidencia.
     * @param search Término de búsqueda opcional.
     * @param pageable Información de paginación.
     * @return Una página de incidencias que coinciden con los filtros.
     */
    @GetMapping("/incidences")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorIncidenceDto>> getIncidences(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID incidenceTypeId,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        return ResponseEntity.ok(
                inspectorService.getFilteredIncidences(status, incidenceTypeId, search, pageable)
        );
    }

    /**
     * Obtiene una lista paginada de cierres diarios dentro de un rango de fechas.
     * @param startDate Fecha de inicio del rango de búsqueda.
     * @param endDate Fecha de fin del rango de búsqueda.
     * @param pageable Información de paginación.
     * @return Una página de cierres diarios.
     */
    @GetMapping("/daily-closures")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorDailyClosureDto>> getDailyClosures(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {

        return ResponseEntity.ok(inspectorService.getDailyClosures(startDate, endDate, pageable));
    }

    /**
     * Obtiene un registro paginado de las auditorías del sistema.
     * Permite filtrar por tipo de acción y rango de fechas.
     * @param action Tipo de acción de la auditoría.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @param pageable Información de paginación.
     * @return Una página de registros de auditoría.
     */
    @GetMapping("/audits")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorAuditDto>> getAudits(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {

        return ResponseEntity.ok(inspectorService.getAudits(action, startDate, endDate, pageable));
    }

    /**
     * Obtiene el detalle de un registro de auditoría específico.
     * @param id El UUID del registro de auditoría.
     * @return Los detalles completos de la auditoría.
     */
    @GetMapping("/audits/{id}")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<InspectorAuditDetailDto> getAuditDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(inspectorService.getAuditDetail(id));
    }
}
