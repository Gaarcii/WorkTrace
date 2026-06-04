package com.worktrace.worktracebackend.service.inspector;

import com.worktrace.worktracebackend.dto.inspector.*;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.*;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio que encapsula la lógica de negocio para el rol de Inspector.
 * Su propósito es proporcionar una capa de acceso de solo lectura a los datos
 * de la plataforma (empleados, fichajes, incidencias, auditorías), permitiendo
 * a un inspector realizar tareas de supervisión y verificación sin capacidad de modificación.
 */
@Service
@RequiredArgsConstructor
public class InspectorService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final IncidenceRepository incidenceRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final AuditTimeEntryRepository auditTimeEntryRepository;
    private final DailyClosureRepository dailyClosureRepository;
    private final VerifyIntegrityUseCase verifyIntegrityUseCase;

    /**
     * Obtiene los datos agregados para la página de inicio del inspector.
     * Este método recopila métricas clave como el número total de empleados, incidencias pendientes
     * y registros de auditoría, proporcionando una vista general y rápida del estado del sistema.
     *
     * @return Un DTO {@link InspectorHomeResponseDto} con las estadísticas principales.
     */
    @Transactional(readOnly = true)
    public InspectorHomeResponseDto getHome() {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();
        LocalDate today = LocalDate.now();

        long totalEmployees = userRepository.countUsersByCompany_IdAndRoleIn(companyId, List.of(Role.WORKER, Role.ADMIN));
        long totalPendingIncidences = incidenceRepository.countByCompany_IdAndStatus(companyId, IncidenceStatus.PENDING);
        long activeWorkersToday = timeEntryRepository.countDistinctActiveWorkersByCompanyAndWorkDate(
                companyId,
                today,
                TimeEntryStatus.OPEN
        );
        long totalAuditLogs = auditTimeEntryRepository.countByCompanyId(companyId);

        return new InspectorHomeResponseDto(
                totalEmployees,
                totalPendingIncidences,
                activeWorkersToday,
                totalAuditLogs
        );
    }

    /**
     * Obtiene una lista paginada de empleados de la empresa.
     * Permite al inspector consultar la plantilla de trabajadores, con la opción de filtrar
     * por nombre o DNI para facilitar la búsqueda de un empleado específico.
     *
     * @param pageable La información de paginación.
     * @param search   Un término de búsqueda opcional para filtrar por nombre o DNI.
     * @return Una página {@link Page} de {@link EmployeeDto} con los datos de los empleados.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployees(Pageable pageable, String search) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        Page<User> users;

        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchByCompanyIdAndRoleAndFullName
                    (companyId, Role.WORKER, search.trim(), pageable);
        } else {
            users = userRepository.findByCompanyIdAndRole(companyId, Role.WORKER, pageable);
        }
        return users.map(user -> new EmployeeDto(
                user.getId(),
                user.getProfile().getAvatarUrl(),
                user.getProfile().getFullName(),
                user.getProfile().getPosition() != null ? user.getProfile().getPosition().getTitle() : null,
                user.getEmail(),
                user.getProfile().getPhone(),
                user.getProfile().getEmployeeCode(),
                user.getRole()
        ));
    }

    /**
     * Obtiene los detalles laborales de un empleado específico.
     * Este método proporciona información detallada sobre un empleado, como sus horarios de trabajo
     * asignados y las horas semanales teóricas, datos clave para una auditoría.
     *
     * @param employeeId El UUID del empleado a consultar.
     * @return Un DTO {@link EmployeeDetailDto} con los detalles del empleado.
     */
    @Transactional(readOnly = true)
    public EmployeeDetailDto getEmployeeDetail(UUID employeeId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        User user = userRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return getEmployeeDetailDto(user);
    }

    private EmployeeDetailDto getEmployeeDetailDto(User user) {
        return new EmployeeDetailDto(
                user.getId(),
                user.getCreatedAt(),
                user.getProfile().getWeeklyHours(),
                user.getProfile().getIsActive(),
                user.getProfile().getWorkSchedules().stream()
                        .map(ws -> {
                            long hours = Duration.between(ws.getStartTime(), ws.getEndTime()).toHours();
                            return new WorkScheduleResponseDto(
                                    ws.getSite().getName(),
                                    ws.getSite().getAddress(),
                                    ws.getDayOfWeek(),
                                    ws.getStartTime(),
                                    ws.getEndTime(),
                                    hours
                            );
                        })
                        .collect(Collectors.toList())
        );
    }

    /**
     * Obtiene una página de incidencias con capacidad de filtrado avanzado.
     * Permite al inspector buscar incidencias por estado (pendiente, resuelta), tipo, o a través
     * de un término de búsqueda de texto libre, facilitando la investigación de casos específicos.
     *
     * @param status          El estado de la incidencia a filtrar (opcional).
     * @param incidenceTypeId El UUID del tipo de incidencia a filtrar (opcional).
     * @param search          Un término de búsqueda de texto libre (opcional).
     * @param pageable        La información de paginación.
     * @return Una página {@link Page} de {@link InspectorIncidenceDto} con las incidencias filtradas.
     */
    @Transactional(readOnly = true)
    public Page<InspectorIncidenceDto> getFilteredIncidences(
            String status,
            UUID incidenceTypeId,
            String search,
            Pageable pageable) {

        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        IncidenceStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = IncidenceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado no válido: " + status);
            }
        }

        Page<Incidence> incidences = incidenceRepository.findFilteredIncidences(
                companyId,
                statusEnum != null ? IncidenceStatus.valueOf(statusEnum.name()) : null,
                incidenceTypeId,
                (search != null && !search.trim().isEmpty()) ? search.trim() : "",
                pageable
        );

        return incidences.map(this::mapToInspectorIncidenceDto);
    }

    private InspectorIncidenceDto mapToInspectorIncidenceDto(Incidence incidence) {
        return new InspectorIncidenceDto(
                incidence.getProfile().getFullName(),
                incidence.getProfile().getUser().getEmail(),
                incidence.getProfile().getAvatarUrl(),
                incidence.getType().getName(),
                incidence.getCreatedAt(),
                incidence.getStatus().name(),
                incidence.getComment(),
                incidence.getResolvedBy() != null ?
                        incidence.getResolvedBy().getProfile().getFullName() : null
        );
    }

    /**
     * Obtiene una lista paginada de los cierres diarios y verifica su integridad.
     * Para cada cierre, recalcula el hash diario y lo compara con el almacenado, permitiendo
     * al inspector detectar de forma inmediata si los registros de un día han sido alterados.
     *
     * @param startDate La fecha de inicio del rango a consultar (opcional).
     * @param endDate   La fecha de fin del rango a consultar (opcional).
     * @param pageable  La información de paginación.
     * @return Una página {@link Page} de {@link InspectorDailyClosureDto} con los cierres y su estado de integridad.
     */
    @Transactional(readOnly = true)
    public Page<InspectorDailyClosureDto> getDailyClosures(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        Page<DailyClosure> closures = dailyClosureRepository.findFilteredClosures(
                companyId,
                startDate,
                endDate,
                pageable
        );

        return closures.map(closure -> new InspectorDailyClosureDto(
                closure.getWorkDate(),
                verifyIntegrityUseCase.execute(companyId, closure.getWorkDate()).name(),
                closure.getRecordsCount(),
                closure.getDayHash(),
                closure.getPrevDayHash(),
                closure.getComputedAt()
        ));
    }

    /**
     * Obtiene una lista paginada de los registros de auditoría del sistema.
     * Permite al inspector filtrar por tipo de acción (p. ej., "UPDATE", "VOID") y por rango de fechas
     * para investigar quién ha modificado qué registros y cuándo.
     *
     * @param action    El tipo de acción a filtrar (opcional).
     * @param startDate La fecha de inicio del rango (opcional).
     * @param endDate   La fecha de fin del rango (opcional).
     * @param pageable  La información de paginación.
     * @return Una página {@link Page} de {@link InspectorAuditDto} con los registros de auditoría.
     */
    @Transactional(readOnly = true)
    public Page<InspectorAuditDto> getAudits(String action, java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        Page<AuditTimeEntry> audits = auditTimeEntryRepository.findFilteredAudits(
                companyId,
                (action != null && !action.trim().isEmpty()) ? action.trim() : null,
                startDate,
                endDate,
                pageable
        );

        return audits.map(audit -> {
            String actorName = audit.getActorUserId() != null
                    ? userRepository.findById(audit.getActorUserId())
                        .map(u -> u.getProfile().getFullName())
                        .orElse("Usuario Desconocido")
                    : "Sistema (Modificación Directa en BD)";

            return new InspectorAuditDto(
                    audit.getId(),
                    audit.getCreatedAt(),
                    audit.getAction(),
                    audit.getReason(),
                    actorName
            );
        });
    }

    /**
     * Obtiene el detalle completo de un registro de auditoría específico.
     * Este método es crucial para la inspección, ya que devuelve no solo quién hizo el cambio y por qué,
     * sino también una instantánea en JSON de los datos *antes* de que se realizara la modificación.
     *
     * @param auditId El UUID del registro de auditoría a consultar.
     * @return Un DTO {@link InspectorAuditDetailDto} con todos los detalles de la modificación.
     */
    @Transactional(readOnly = true)
    public InspectorAuditDetailDto getAuditDetail(UUID auditId) {
        UserAndCompanyInfo info = userService.getAuthenticatedUserAndCompanyInfo();
        var companyId = info.getCompany().getId();

        AuditTimeEntry audit = auditTimeEntryRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Registro de auditoría no encontrado"));

        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("No tienes permiso para ver este registro");
        }

        String actorName = audit.getActorUserId() != null
                ? userRepository.findById(audit.getActorUserId())
                    .map(u -> u.getProfile().getFullName())
                    .orElse("Usuario Desconocido")
                : "Modificación Directa en BD";

        return new InspectorAuditDetailDto(
                audit.getId(),
                audit.getCreatedAt(),
                audit.getReason(),
                actorName,
                audit.getOldData()
        );
    }
}