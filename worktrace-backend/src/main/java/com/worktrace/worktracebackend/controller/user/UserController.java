package com.worktrace.worktracebackend.controller.user;

import com.worktrace.worktracebackend.dto.user.DepartmentStatDto;
import com.worktrace.worktracebackend.dto.user.EditEmployeeWorkDataRequestDto;
import com.worktrace.worktracebackend.dto.user.EmployeeResponseDto;
import com.worktrace.worktracebackend.dto.user.UserRequestDto;
import com.worktrace.worktracebackend.dto.user.UserResponseDto;
import com.worktrace.worktracebackend.service.user.UserProfileService;
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
 * Controlador para gestionar la información de los usuarios y empleados.
 * Permite a los usuarios obtener y actualizar su propio perfil, y a los administradores
 * gestionar los datos de los empleados de su empresa.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    /**
     * Obtiene el perfil del usuario actualmente autenticado.
     * Sirve para que cualquier usuario pueda ver sus propios datos.
     * @return Un DTO con la información del perfil del usuario.
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getUserProfile() {
        UserResponseDto responseDto = userProfileService.getProfile();
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Actualiza el perfil del usuario actualmente autenticado.
     * Permite a los usuarios modificar sus datos personales, como nombre o foto de perfil.
     * @param requestDto Los datos a actualizar, incluyendo opcionalmente una nueva foto de perfil.
     * @return El perfil del usuario actualizado.
     */
    @PatchMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Valid @ModelAttribute UserRequestDto requestDto) {

        UserResponseDto responseDto = userProfileService.updateProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene el número total de empleados en la empresa del administrador.
     * Útil para mostrar estadísticas rápidas en el dashboard del administrador.
     * @return El número total de empleados.
     */
    @GetMapping("/total-workers")
    @PreAuthorize("hasRole('ADMIN')")
    public Long getTotalWorkers() {
        return userProfileService.countCompanyEmployees();
    }

    /**
     * Obtiene estadísticas sobre la distribución de empleados por departamento.
     * Proporciona una visión general de la estructura de la plantilla de la empresa.
     * @return Una lista de DTOs, cada uno representando un departamento y su número de empleados.
     */
    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentStatDto>> getDepartmentStats() {
        List<DepartmentStatDto> dto = userProfileService.getDepartmentStats();
        return ResponseEntity.ok(dto);
    }

    /**
     * Obtiene una lista paginada de todos los empleados de la empresa del administrador.
     * Permite a los administradores ver y gestionar su lista de empleados.
     * @param pageable Información para la paginación de resultados.
     * @return Una página de DTOs de empleados.
     */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployees(Pageable pageable) {
        Page<EmployeeResponseDto> responseDto = userProfileService.getEmployeesByCompany(pageable);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene los detalles de un empleado específico por su ID.
     * @param id El UUID del empleado a consultar.
     * @return Un DTO con los detalles del empleado.
     */
    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> getEmployeeDetail(@PathVariable UUID id) {
        EmployeeResponseDto responseDto = userProfileService.getEmployeeById(id);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Permite a un administrador editar los datos laborales de un empleado.
     * Esto incluye el puesto de trabajo, el departamento y el tipo de jornada.
     * @param id El UUID del empleado a modificar.
     * @param dto Los nuevos datos laborales para el empleado.
     * @return Una respuesta vacía si la operación fue exitosa.
     */
    @PatchMapping("/employees/{id}/work-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> editEmployeeWorkData(
            @PathVariable UUID id,
            @Valid @RequestBody EditEmployeeWorkDataRequestDto dto) {
        userProfileService.editEmployeeWorkData(id, dto);
        return ResponseEntity.ok().build();
    }
}
