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

/**
 * Controlador para gestionar los puestos de trabajo de una empresa.
 * Permite a los administradores crear, obtener y eliminar los puestos de trabajo
 * que pueden ser asignados a los empleados.
 */
@RestController
@RequestMapping("/api/job-positions")
@RequiredArgsConstructor
public class JobPositionController {
    private final JobPositionService jobPositionService;

    /**
     * Obtiene todos los puestos de trabajo de la empresa del administrador autenticado.
     * Sirve para listar los puestos disponibles que se pueden asignar a los empleados.
     *
     * @return Una lista con todos los puestos de trabajo de la empresa.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobPositionResponseDto>> getJobPositions() {
        List<JobPositionResponseDto> responseDtos = jobPositionService.getJobPositions();
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Crea un nuevo puesto de trabajo para la empresa del administrador autenticado.
     * Permite definir un nuevo rol o cargo que un empleado puede ocupar.
     * @param requestDto Los datos del puesto de trabajo a crear.
     * @return El puesto de trabajo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPositionResponseDto> createJobPosition(
            @RequestBody JobPositionRequestDto requestDto) {
        JobPositionResponseDto responseDto = jobPositionService.createJobPosition(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Elimina un puesto de trabajo por su ID.
     * Esta acción solo es posible si ningún empleado está actualmente asignado a este puesto.
     * @param jobPositionId El ID del puesto de trabajo a eliminar.
     * @return Una respuesta vacía si la eliminación fue exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobPosition(@PathVariable("id") UUID jobPositionId) {
        jobPositionService.deleteJobPosition(jobPositionId);
        return ResponseEntity.ok().build();
    }
}
