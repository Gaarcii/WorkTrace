package com.worktrace.worktracebackend.controller.workSite;


import com.worktrace.worktracebackend.dto.workSite.WorkSiteRequestDto;
import com.worktrace.worktracebackend.dto.workSite.WorkSiteResponseDto;
import com.worktrace.worktracebackend.service.workSite.WorkSiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador para gestionar los centros de trabajo de una empresa.
 * Permite a los administradores crear, obtener, actualizar y eliminar los centros de trabajo
 * donde los empleados pueden realizar sus fichajes.
 */
@RestController
@RequestMapping("/api/work-sites")
@RequiredArgsConstructor
public class WorkSiteController {

    private final WorkSiteService workSiteService;

    /**
     * Obtiene todos los centros de trabajo de la empresa del administrador autenticado.
     * Sirve para listar las ubicaciones disponibles para la asignación de horarios y fichajes.
     * @return Una lista con todos los centros de trabajo de la empresa.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkSiteResponseDto>> getWorkSites() {
        return ResponseEntity.ok(workSiteService.getMyWorkSites());
    }

    /**
     * Crea un nuevo centro de trabajo para la empresa.
     * Permite definir una nueva ubicación física (con coordenadas y radio) donde los empleados pueden trabajar.
     * @param dto Los datos del centro de trabajo a crear.
     * @return El centro de trabajo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSiteResponseDto> createWorkSite
            (@Valid @RequestBody WorkSiteRequestDto dto) {
        WorkSiteResponseDto created = workSiteService.createWorkSite(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza la información de un centro de trabajo existente.
     * Útil para modificar el nombre, la dirección o los parámetros de geolocalización de un centro.
     * @param id El UUID del centro de trabajo a actualizar.
     * @param dto Los nuevos datos para el centro de trabajo.
     * @return El centro de trabajo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSiteResponseDto> updateWorkSite(
            @PathVariable UUID id,
            @Valid @RequestBody WorkSiteRequestDto dto) {

        WorkSiteResponseDto updated = workSiteService.updateWorkSite(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un centro de trabajo por su ID.
     * Esta acción debe usarse con precaución, ya que puede afectar la capacidad de los empleados para fichar
     * si están asignados a este centro.
     * @param id El UUID del centro de trabajo a eliminar.
     * @return Una respuesta vacía si la eliminación fue exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkSite(@PathVariable UUID id) {
        workSiteService.deleteWorkSite(id);
        return ResponseEntity.noContent().build();
    }

}
