package com.worktrace.worktracebackend.service.inspector;

import com.worktrace.worktracebackend.dto.inspector.EmpleadoDetalleDto;
import com.worktrace.worktracebackend.dto.inspector.EmpleadoDto;
import com.worktrace.worktracebackend.dto.inspector.InspectorHomeResponseDto;
import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import com.worktrace.worktracebackend.model.EstadoFichaje;
import com.worktrace.worktracebackend.model.Role;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectorService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final IncidenceRepository incidenceRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final AuditTimeEntryRepository auditTimeEntryRepository;

    @Transactional(readOnly = true)
    public InspectorHomeResponseDto getHome() {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        var companyId = info.getCompany().getId();
        LocalDate today = LocalDate.now();

        long totalEmpleados = userRepository.countUsersByCompany_Id(companyId);
        long totalIncidencias = incidenceRepository.countByCompany_Id(companyId);
        long trabajadoresActivosHoy = timeEntryRepository.countDistinctActiveWorkersByCompanyAndWorkDate(
                companyId,
                today,
                EstadoFichaje.OPEN
        );
        long totalAuditLogs = auditTimeEntryRepository.countByCompanyId(companyId);

        return new InspectorHomeResponseDto(
                totalEmpleados,
                totalIncidencias,
                trabajadoresActivosHoy,
                totalAuditLogs
        );
    }

    @Transactional(readOnly = true)
    public Page<EmpleadoDto> getEmpleados(Pageable pageable, String search) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        var companyId = info.getCompany().getId();

        Page<User> users;

        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchByCompanyIdAndRoleAndFullName
                    (companyId, Role.WORKER, search.trim(), pageable);
        } else {
            users = userRepository.findByCompanyIdAndRole(companyId, Role.WORKER, pageable);
        }
        return users.map(user -> EmpleadoDto.builder()
                .id(user.getId())
                .photo(user.getProfile().getAvatarUrl())
                .name(user.getProfile().getFullName())
                .jobPosition(user.getProfile().getPosition() != null ?
                        user.getProfile().getPosition().getTitle() : null)
                .email(user.getEmail())
                .phone(user.getProfile().getPhone())
                .employeeCode(user.getProfile().getEmployeeCode())
                .role(user.getRole())
                .build());
    }

    @Transactional(readOnly = true)
    public EmpleadoDetalleDto getEmpleadoDetalle(UUID empleadoId) {
        UsuarioYCompaniaInfo info = userService.extraerUsuarioYCompania();
        var companyId = info.getCompany().getId();

        User user = userRepository.findByIdAndCompanyId(empleadoId, companyId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return getEmpleadoDetalleDto(user);
    }

    private EmpleadoDetalleDto getEmpleadoDetalleDto(User user) {
        return EmpleadoDetalleDto.builder()
                .id(user.getId())
                .registrationDate(user.getCreatedAt())
                .weeklyHours(user.getProfile().getWeeklyHours())
                .isActive(user.getProfile().getIsActive())
                .workSchedules(user.getProfile().getWorkSchedules().stream()
                        .map(ws -> {
                            long horas = Duration.between(ws.getStartTime(), ws.getEndTime()).toHours();
                            return new WorkScheduleResponseDto(
                                    ws.getSite().getName(),
                                    ws.getSite().getAddress(),
                                    ws.getDayOfWeek(),
                                    ws.getStartTime(),
                                    ws.getEndTime(),
                                    horas
                            );
                        })
                        .collect(Collectors.toList()))
                .build();
    }

}
