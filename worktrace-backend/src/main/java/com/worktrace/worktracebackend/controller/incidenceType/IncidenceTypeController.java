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

/**
 * Controlador para gestionar los tipos de incidencia.
 * Permite a los usuarios autenticados obtener los tipos de incidencia
 * y a los administradores crear, actualizar y eliminar tipos de incidencia.
 */
@RestController
@RequestMapping("/api/incidence-types")
@RequiredArgsConstructor
public class IncidenceTypeController {

    private final IncidenceTypeService incidenceTypeService;

    /**
     * Obtiene todos los tipos de incidencia disponibles en el sistema.
     * @return Una respuesta que contiene una lista de los tipos de incidencia.
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncidenceTypeResponseDto> getIncidenceTypes() {
        IncidenceTypeResponseDto responseDto = incidenceTypeService
                .getIncidenceTypes();
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Crea un nuevo tipo de incidencia.
     * Solo los administradores pueden realizar esta operación para definir nuevas categorías de incidencias.
     * @param dto Los datos del tipo de incidencia a crear.
     * @return El tipo de incidencia creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidenceTypeItemDto> createIncidenceType(
            @Valid @RequestBody IncidenceTypeRequestDto dto) {
        IncidenceTypeItemDto created = incidenceTypeService.createIncidenceType(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un tipo de incidencia existente.
     * Solo los administradores pueden modificar los detalles de un tipo de incidencia.
     * @param id El identificador único del tipo de incidencia a actualizar.
     * @param dto Los nuevos datos para el tipo de incidencia.
     * @return El tipo de incidencia actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidenceTypeItemDto> updateIncidenceType(
            @PathVariable UUID id,
            @Valid @RequestBody IncidenceTypeRequestDto dto) {
        IncidenceTypeItemDto updated = incidenceTypeService.updateIncidenceType(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un tipo de incidencia existente.
     * Solo los administradores pueden eliminar tipos de incidencia.
     * @param id El identificador único del tipo de incidencia a eliminar.
     * @return Una respuesta vacía indicando que la operación fue exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIncidenceType(@PathVariable UUID id) {
        incidenceTypeService.deleteIncidenceType(id);
        return ResponseEntity.noContent().build();
    }

}
