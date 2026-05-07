package com.worktrace.worktracebackend.service.jobPosition;

import com.worktrace.worktracebackend.dto.jobPosition.JobPositionRequestDto;
import com.worktrace.worktracebackend.dto.jobPosition.JobPositionResponseDto;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.JobPosition;
import com.worktrace.worktracebackend.repository.JobPositionRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio de los puestos de trabajo de una empresa.
 * Su propósito es permitir a los administradores definir, consultar y eliminar los diferentes
 * cargos o roles que los empleados pueden ocupar, estructurando así la plantilla de la organización.
 */
@Service
@RequiredArgsConstructor
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;
    private final UserService userService;

    /**
     * Obtiene todos los puestos de trabajo definidos para la empresa del usuario autenticado.
     * Este método es fundamental para poblar los selectores en la interfaz de usuario, por ejemplo,
     * al registrar un nuevo empleado o al modificar el puesto de uno existente.
     *
     * @return Una lista de {@link JobPositionResponseDto} que representa los puestos de trabajo disponibles.
     */
    @Transactional(readOnly = true)
    public List<JobPositionResponseDto> getJobPositions() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<JobPosition> jobPositionList = jobPositionRepository
                .findByCompany_Id(info.getCompany().getId());

        return jobPositionList.stream().map(
                position -> new JobPositionResponseDto(
                        position.getId(),
                        position.getTitle()
                )).toList();
    }

    /**
     * Crea un nuevo puesto de trabajo para la empresa del administrador.
     * Antes de la creación, se asegura de que no exista ya un puesto con el mismo nombre (ignorando mayúsculas/minúsculas)
     * para evitar duplicados y mantener la consistencia de los datos.
     *
     * @param requestDto El DTO que contiene el nombre del puesto de trabajo a crear.
     * @return Un {@link JobPositionResponseDto} que representa el puesto de trabajo recién creado.
     */
    @Transactional
    public JobPositionResponseDto createJobPosition(JobPositionRequestDto requestDto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        String name = requestDto.getName().trim();
        boolean exists = jobPositionRepository.findByTitleIgnoreCaseAndCompany_Id(name, info.getCompany().getId()).isPresent();
        if (exists) {
            throw new IllegalArgumentException("Ya existe un puesto con ese nombre en la empresa");
        }
        JobPosition jobPosition = new JobPosition();
        jobPosition.setTitle(name);
        jobPosition.setCreatedAt(OffsetDateTime.now());
        jobPosition.setCompany(info.getCompany());
        jobPositionRepository.save(jobPosition);

        return new JobPositionResponseDto(
                jobPosition.getId(),
                jobPosition.getTitle());
    }

    /**
     * Elimina un puesto de trabajo por su ID.
     * La operación solo se permite si el puesto de trabajo pertenece a la empresa del administrador que realiza la petición.
     * La base de datos impedirá la eliminación si el puesto está asignado a algún empleado para mantener la integridad referencial.
     *
     * @param jobPositionId El UUID del puesto de trabajo que se va a eliminar.
     */
    @Transactional
    public void deleteJobPosition(UUID jobPositionId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();

        JobPosition jobPosition = jobPositionRepository.findById(jobPositionId)
                .orElseThrow(() -> new IllegalStateException("Puesto de trabajo no encontrado"));

        if (jobPosition.getCompany().getId().equals(company.getId())) {
            jobPositionRepository.delete(jobPosition);
        }
    }
}
