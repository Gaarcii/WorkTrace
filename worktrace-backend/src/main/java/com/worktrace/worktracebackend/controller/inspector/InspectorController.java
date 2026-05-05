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

    @GetMapping("/employees")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<EmployeeDto>> getEmployees(
            Pageable pageable, 
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inspectorService.getEmployees(pageable, search));
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<EmployeeDetailDto> getEmployeeDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(inspectorService.getEmployeeDetail(id));
    }

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

    @GetMapping("/daily-closures")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorDailyClosureDto>> getDailyClosures(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {

        return ResponseEntity.ok(inspectorService.getDailyClosures(startDate, endDate, pageable));
    }

    @GetMapping("/audits")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<Page<InspectorAuditDto>> getAudits(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {

        return ResponseEntity.ok(inspectorService.getAudits(action, startDate, endDate, pageable));
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasRole('INSPECTOR')")
    public ResponseEntity<InspectorAuditDetailDto> getAuditDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(inspectorService.getAuditDetail(id));
    }
}
