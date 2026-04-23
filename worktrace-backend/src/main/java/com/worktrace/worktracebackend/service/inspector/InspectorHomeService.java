package com.worktrace.worktracebackend.service.inspector;

import com.worktrace.worktracebackend.dto.inspector.InspectorHomeResponseDto;
import com.worktrace.worktracebackend.model.EstadoFichaje;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.auth.UsuarioYCompaniaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InspectorHomeService {

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
}


