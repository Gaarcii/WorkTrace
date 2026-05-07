package com.worktrace.worktracebackend.controller.workSchedule;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleRequestDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.service.workSchedule.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador para gestionar los horarios de trabajo de los empleados.
 * Permite a los administradores asignar y consultar los horarios de trabajo.
 */
@RestController
@RequestMapping("/api/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    /**
     * Asigna un horario de trabajo a uno o varios empleados.
     * Esta operación permite a los administradores definir los turnos y jornadas laborales.
     * @param dto El DTO que contiene la información del horario y los empleados a los que se asigna.
     * @return Una respuesta vacía si la asignación fue exitosa.
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignWorkSchedule(
            @Valid @RequestBody WorkScheduleRequestDto dto) {
        workScheduleService.assignWorkSchedule(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene todos los horarios de trabajo asignados a un empleado específico.
     * @param employeeId El UUID del empleado cuyos horarios se quieren consultar.
     * @return Una lista con los horarios de trabajo del empleado.
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkScheduleResponseDto>> getEmployeeSchedules(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(workScheduleService.getEmployeeSchedules(employeeId));
    }
}
