package com.worktrace.worktracebackend.service.incidenceType;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeItemDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeRequestDto;
import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.IncidenceType;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio de los tipos de incidencia.
 * Permite a los administradores de una empresa definir y mantener las categorías
 * que los empleados pueden usar para reportar incidencias (p. ej., "Olvido de fichaje", "Error en fichaje").
 */
@Service
@RequiredArgsConstructor
public class IncidenceTypeService {
    private final IncidenceTypeRepository incidenceTypeRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserService userService;

    /**
     * Obtiene todos los tipos de incidencia activos para la empresa del usuario autenticado.
     * Se utiliza para poblar listas o selectores en la interfaz de usuario donde el empleado
     * necesita elegir un tipo de incidencia al crear una nueva.
     *
     * @return Un DTO que contiene una lista de los tipos de incidencia disponibles.
     */
    @Transactional(readOnly = true)
    public IncidenceTypeResponseDto getIncidenceTypes() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        IncidenceTypeResponseDto typeResponseDto = new IncidenceTypeResponseDto();
        typeResponseDto.setTypes(
                incidenceTypeRepository.findByCompany_IdAndDeletedAtIsNull(
                        info.getCompany().getId()));

        return typeResponseDto;
    }

    /**
     * Crea un nuevo tipo de incidencia para la empresa del administrador.
     * Si ya existe un tipo con el mismo nombre que fue eliminado lógicamente (soft delete),
     * este método lo reactiva en lugar de crear un duplicado. Esto mantiene la consistencia
     * de los datos y evita la proliferación de categorías redundantes.
     *
     * @param dto El DTO con el nombre del tipo de incidencia a crear.
     * @return Un DTO que representa el tipo de incidencia creado o reactivado.
     */
    @Transactional
    public IncidenceTypeItemDto createIncidenceType(IncidenceTypeRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        String name = dto.getName().trim();
        IncidenceType existing = incidenceTypeRepository
                .findByNameIgnoreCaseAndCompany_Id(name, company.getId())
                .orElse(null);

        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un tipo de incidencia con ese nombre"
                );
            }

            existing.setDeletedAt(null);
            existing.setName(name);
            IncidenceType reactivated = incidenceTypeRepository.save(existing);
            return new IncidenceTypeItemDto(reactivated.getId(), reactivated.getName());
        }

        IncidenceType incidenceType = new IncidenceType();
        incidenceType.setName(name);
        incidenceType.setCompany(company);
        incidenceType.setCreatedAt(OffsetDateTime.now());

        IncidenceType saved = incidenceTypeRepository.save(incidenceType);
        return new IncidenceTypeItemDto(saved.getId(), saved.getName());
    }

    /**
     * Actualiza el nombre de un tipo de incidencia existente.
     * Antes de actualizar, verifica que no exista ya otro tipo de incidencia con el nuevo nombre
     * para evitar duplicados que puedan confundir a los usuarios.
     *
     * @param id  El UUID del tipo de incidencia a actualizar.
     * @param dto El DTO con el nuevo nombre para el tipo de incidencia.
     * @return Un DTO que representa el tipo de incidencia con su nombre actualizado.
     */
    @Transactional
    public IncidenceTypeItemDto updateIncidenceType(UUID id, IncidenceTypeRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        IncidenceType incidenceType = incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, company.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El tipo de incidencia no existe o no pertenece a tu empresa"
                ));

        String name = dto.getName().trim();
        incidenceTypeRepository.findByNameIgnoreCaseAndCompany_Id(name, company.getId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un tipo de incidencia con ese nombre"
                    );
                });

        incidenceType.setName(name);
        IncidenceType updated = incidenceTypeRepository.save(incidenceType);

        return new IncidenceTypeItemDto(updated.getId(), updated.getName());
    }

    /**
     * Elimina un tipo de incidencia.
     * La estrategia de eliminación depende de si el tipo de incidencia ha sido utilizado:
     * - Si ya existen incidencias de este tipo, se realiza un borrado lógico (soft delete)
     *   marcando el `deletedAt` para mantener la integridad referencial de los registros históricos.
     * - Si nunca se ha usado, se realiza un borrado físico (hard delete) de la base de datos.
     *
     * @param id El UUID del tipo de incidencia a eliminar.
     */
    @Transactional
    public void deleteIncidenceType(UUID id) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        IncidenceType incidenceType = incidenceTypeRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, company.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El tipo de incidencia no existe o no pertenece a tu empresa"
                ));

        boolean hasIncidences = incidenceRepository.existsByType_IdAndCompany_Id(incidenceType.getId(), company.getId());

        if (hasIncidences) {
            incidenceType.setDeletedAt(OffsetDateTime.now());
            incidenceTypeRepository.save(incidenceType);
        } else {
            incidenceTypeRepository.delete(incidenceType);
        }
    }
}
