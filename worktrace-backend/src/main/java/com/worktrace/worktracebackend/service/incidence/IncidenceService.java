package com.worktrace.worktracebackend.service.incidence;

import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.AdminIncidenceResponseDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceRequestDto;
import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.IncidenceTypeRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio de las incidencias de los empleados.
 * Se encarga de la creación, consulta y gestión (aprobación/rechazo) de las incidencias
 * reportadas por los trabajadores, como olvidos de fichaje o errores en el registro horario.
 */
@Service
@RequiredArgsConstructor
public class IncidenceService {

    private final IncidenceTypeRepository incidenceTypeRepository;
    private final IncidenceRepository incidenceRepository;
    private final UserService userService;

    /**
     * Obtiene todas las incidencias registradas por el usuario actualmente autenticado.
     * Permite a un trabajador consultar el historial y el estado de sus propias solicitudes.
     * @return Una lista de DTOs con la información de las incidencias del trabajador.
     */
    @Transactional(readOnly = true)
    public List<WorkerIncidenceResponseDto> getIncidencesByUserId() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<Incidence> incidenceList = incidenceRepository
                .findByProfile_UserId(info.getUser().getId());

        return mapToWorkerIncidenceResponseDtos(incidenceList);
    }

    /**
     * Crea una nueva incidencia para el trabajador autenticado.
     * El trabajador puede reportar un evento, como un olvido de fichaje, que quedará
     * pendiente de revisión por parte de un administrador.
     * @param requestDto Los datos de la incidencia a crear.
     * @return Un DTO con la información de la incidencia recién creada.
     */
    @Transactional
    public WorkerIncidenceResponseDto createIncidence(WorkerIncidenceRequestDto requestDto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        IncidenceType incidenceType = incidenceTypeRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(requestDto.getTypeId(), info.getCompany().getId())
                .orElseThrow(() -> new IllegalStateException("Tipo de incidencia no encontrado"));

        Incidence incidence = new Incidence();
        incidence.setProfile(info.getProfile());
        incidence.setDate(requestDto.getAffectedDate());
        incidence.setComment(requestDto.getComment());
        incidence.setStatus(IncidenceStatus.PENDING);
        incidence.setCreatedAt(OffsetDateTime.now());
        incidence.setType(incidenceType);
        incidence.setIncidenceTime(requestDto.getTime());
        incidence.setCompany(info.getCompany());
        incidence.setUpdatedAt(OffsetDateTime.now());

        incidence = incidenceRepository.save(incidence);
        return new WorkerIncidenceResponseDto(
                incidence.getType().getName(),
                incidence.getDate(),
                incidence.getIncidenceTime(),
                incidence.getComment(),
                incidence.getStatus(),
                incidence.getCreatedAt()
        );
    }

    /**
     * Obtiene las incidencias del usuario autenticado dentro de un rango de fechas específico.
     * @param startDate La fecha de inicio del rango.
     * @param endDate La fecha de fin del rango.
     * @return Una lista de DTOs con las incidencias encontradas en ese período.
     */
    @Transactional(readOnly = true)
    public List<WorkerIncidenceResponseDto> getIncidencesByDateRange(LocalDate startDate, LocalDate endDate) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        List<Incidence> incidenceList = incidenceRepository
                .getIncidencesByProfile_UserIdAndDateBetween(
                        info.getUser().getId(),
                        startDate, endDate);

        return mapToWorkerIncidenceResponseDtos(incidenceList);
    }

    /**
     * Obtiene una página de incidencias de la empresa, filtradas por estado.
     * Este método es utilizado por los administradores para revisar las incidencias
     * que están, por ejemplo, pendientes de gestión.
     * @param status El estado por el cual filtrar las incidencias (PENDING, RESOLVED, REJECTED).
     * @param pageable La información de paginación.
     * @return Una página de DTOs con las incidencias correspondientes.
     */
    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getCompanyIncidencesByStatus(IncidenceStatus status, Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();

        Page<Incidence> incidencePage = incidenceRepository
                .findByCompany_IdAndStatus(info.getCompany().getId(), status, pageable);

        return mapToAdminIncidenceResponseDtoPage(incidencePage);
    }

    /**
     * Obtiene el historial de incidencias ya gestionadas (resueltas o rechazadas) de la empresa.
     * Permite a los administradores consultar un registro de las decisiones tomadas.
     * @param pageable La información de paginación.
     * @return Una página de DTOs con el historial de incidencias.
     */
    @Transactional(readOnly = true)
    public Page<AdminIncidenceResponseDto> getCompanyIncidenceHistory(Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        Page<Incidence> incidencePage = incidenceRepository.findByCompany_IdAndStatusIn
                (company.getId(), List.of(IncidenceStatus.RESOLVED, IncidenceStatus.REJECTED), pageable);

        return mapToAdminIncidenceResponseDtoPage(incidencePage);
    }

    /**
     * Permite a un administrador gestionar una incidencia pendiente.
     * La gestión implica cambiar el estado de la incidencia a "resuelta" o "rechazada"
     * y añadir una respuesta o comentario para el empleado.
     * @param incidenceId El ID de la incidencia a gestionar.
     * @param dto El DTO con el nuevo estado y la respuesta del administrador.
     */
    @Transactional
    public void manageIncidence(UUID incidenceId, AdminIncidenceRequestDto dto) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        Company company = info.getCompany();
        User user = info.getUser();

        Incidence incidence = incidenceRepository.findById(incidenceId)
                .orElseThrow(() -> new IllegalStateException("Incidencia no encontrada"));

        if (!incidence.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("No tienes permisos sobre esta incidencia");
        }

        if (incidence.getStatus() == IncidenceStatus.PENDING) {
            incidence.setStatus(dto.getStatus());
            incidence.setUpdatedAt(OffsetDateTime.now());
            incidence.setResolvedBy(user);
            incidence.setAdminResponse(dto.getAdminResponse());

            incidenceRepository.save(incidence);
        } else {
            throw new IllegalStateException("La incidencia ya ha sido gestionada anteriormente");
        }
    }

    private List<WorkerIncidenceResponseDto> mapToWorkerIncidenceResponseDtos(List<Incidence> incidenceList) {
        return incidenceList.stream()
                .map(incident -> new WorkerIncidenceResponseDto(
                        incident.getType().getName(),
                        incident.getDate(),
                        incident.getIncidenceTime(),
                        incident.getComment(),
                        (incident.getStatus()),
                        incident.getCreatedAt()
                ))
                .toList();
    }

    private Page<AdminIncidenceResponseDto> mapToAdminIncidenceResponseDtoPage(Page<Incidence> incidencePage) {
        return incidencePage.map(incident -> new AdminIncidenceResponseDto(
                incident.getId(),
                incident.getProfile().getFullName(),
                getJobPositionTitle(incident),
                incident.getType().getName(),
                incident.getComment(),
                incident.getStatus(),
                incident.getDate(),
                incident.getCreatedAt(),
                incident.getProfile().getAvatarUrl(),
                incident.getAdminResponse()
        ));
    }

    private String getJobPositionTitle(Incidence incident) {
        if (incident.getProfile() == null || incident.getProfile().getPosition() == null) {
            return "Sin asignar";
        }
        return incident.getProfile().getPosition().getTitle();
    }
}
