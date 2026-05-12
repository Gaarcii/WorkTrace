package com.worktrace.worktracebackend.service.inspector;

import com.worktrace.worktracebackend.dto.inspector.EmployeeDto;
import com.worktrace.worktracebackend.dto.inspector.InspectorHomeResponseDto;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.IncidenceRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectorServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IncidenceRepository incidenceRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private AuditTimeEntryRepository auditTimeEntryRepository;

    @InjectMocks
    private InspectorService inspectorService;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        User inspectorUser = new User();
        inspectorUser.setRole(Role.INSPECTOR);
        inspectorUser.setCompany(company);

        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(inspectorUser, company, null);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
    }

    @Test
    void testGetHomeSuccess() {
        when(userRepository.countUsersByCompany_Id(companyId)).thenReturn(10L);
        when(incidenceRepository.countByCompany_IdAndStatus(companyId, IncidenceStatus.PENDING)).thenReturn(5L);
        when(timeEntryRepository.countDistinctActiveWorkersByCompanyAndWorkDate(eq(companyId), any(LocalDate.class), eq(TimeEntryStatus.OPEN))).thenReturn(8L);
        when(auditTimeEntryRepository.countByCompanyId(companyId)).thenReturn(100L);

        InspectorHomeResponseDto response = inspectorService.getHome();

        assertNotNull(response);
        assertEquals(10L, response.getTotalEmployees());
        assertEquals(5L, response.getTotalIncidences());
        assertEquals(8L, response.getActiveEmployeesToday());
        assertEquals(100L, response.getTotalAuditLogs());
    }

    @Test
    void testGetEmployeesWithSearch() {
        User worker = new User();
        worker.setId(UUID.randomUUID());
        worker.setEmail("juan@test.com");
        worker.setRole(Role.WORKER);
        Profile profile = new Profile();
        profile.setFullName("Juan Buscado");
        profile.setEmployeeCode("EMP001");
        worker.setProfile(profile);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(worker));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.searchByCompanyIdAndRoleAndFullName(companyId, Role.WORKER, "Juan", pageable)).thenReturn(userPage);

        Page<EmployeeDto> result = inspectorService.getEmployees(pageable, "Juan");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Juan Buscado", result.getContent().getFirst().getName());
    }

    @Test
    void testGetEmployeesWithoutSearch() {
        User worker = new User();
        worker.setId(UUID.randomUUID());
        worker.setEmail("ana@test.com");
        worker.setRole(Role.WORKER);
        Profile profile = new Profile();
        profile.setFullName("Ana General");
        profile.setEmployeeCode("EMP002");
        worker.setProfile(profile);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(worker));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByCompanyIdAndRole(companyId, Role.WORKER, pageable)).thenReturn(userPage);

        Page<EmployeeDto> result = inspectorService.getEmployees(pageable, null);

        assertFalse(result.isEmpty());
        assertEquals("Ana General", result.getContent().getFirst().getName());
    }

    @Test
    void testGetAuditDetailSuccess() {
        UUID auditId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AuditTimeEntry audit = new AuditTimeEntry();
        audit.setId(auditId);
        audit.setCompanyId(companyId);
        audit.setActorUserId(actorId);
        audit.setReason("Corrección manual de fichaje");
        audit.setOldData("{\"endAt\":\"2023-01-01T17:00:00Z\"}");

        User actor = new User();
        actor.setId(actorId);
        Profile actorProfile = new Profile();
        actorProfile.setFullName("Admin Responsable");
        actor.setProfile(actorProfile);

        when(auditTimeEntryRepository.findById(auditId)).thenReturn(Optional.of(audit));
        when(userRepository.findById(actorId)).thenReturn(Optional.of(actor));

        var result = inspectorService.getAuditDetail(auditId);

        assertNotNull(result);
        assertEquals(auditId, result.getId());
        assertEquals("Admin Responsable", result.getActorName());
        assertEquals("Corrección manual de fichaje", result.getReason());
        assertEquals("{\"endAt\":\"2023-01-01T17:00:00Z\"}", result.getPreviousData());
    }

    @Test
    void testGetAuditDetailThrowsExceptionWhenNotFound() {
        when(auditTimeEntryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> inspectorService.getAuditDetail(UUID.randomUUID()));

        assertEquals("Registro de auditoría no encontrado", exception.getMessage());
    }

    @Test
    void testGetAuditDetailThrowsExceptionForCompanyMismatch() {
        UUID auditId = UUID.randomUUID();
        AuditTimeEntry audit = new AuditTimeEntry();
        audit.setId(auditId);
        audit.setCompanyId(UUID.randomUUID());

        when(auditTimeEntryRepository.findById(auditId)).thenReturn(Optional.of(audit));

        Exception exception = assertThrows(RuntimeException.class, () -> inspectorService.getAuditDetail(auditId));

        assertEquals("No tienes permiso para ver este registro", exception.getMessage());
    }
}
