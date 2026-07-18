package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.dto.timeEntry.EditTimeEntryRequestDto;
import com.worktrace.worktracebackend.dto.timeEntry.VoidTimeEntryRequestDto;
import com.worktrace.worktracebackend.exception.NotFoundException;
import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auditTimeEntry.AuditTimeEntryService;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuditTimeEntryService auditTimeEntryService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private User user;
    private TimeEntry openTimeEntry;
    private UserAndCompanyInfo userAndCompanyInfo;
    private Company company;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        company = new Company();
        company.setId(companyId);
        company.setCompanyName("Test Company");

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setCompany(company);
        user.setRole(Role.WORKER);

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFullName("Test User");
        profile.setUser(user);

        userAndCompanyInfo = new UserAndCompanyInfo(user, company, profile);

        openTimeEntry = new TimeEntry();
        openTimeEntry.setId(UUID.randomUUID());
        openTimeEntry.setEmployee(profile);
        openTimeEntry.setCompany(company);
        openTimeEntry.setStartAt(OffsetDateTime.now().minusHours(1));
        openTimeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);
    }

    @Test
    void testUpdateTimeEntrySuccess() {
        UUID timeEntryId = openTimeEntry.getId();
        OffsetDateTime newStartAt = OffsetDateTime.now().minusHours(2);
        OffsetDateTime newEndAt = OffsetDateTime.now().minusHours(1);
        String justification = "Corrección de hora de entrada y salida.";

        EditTimeEntryRequestDto dto = new EditTimeEntryRequestDto(newStartAt, newEndAt, justification);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(openTimeEntry));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        timeEntryService.updateTimeEntry(timeEntryId, dto);

        assertAll(
                () -> assertEquals(newStartAt, openTimeEntry.getStartAt()),
                () -> assertEquals(newEndAt, openTimeEntry.getEndAt()),
                () -> assertEquals(TimeEntryStatus.CLOSED, openTimeEntry.getTimeEntryStatus()),
                () -> assertEquals(justification, openTimeEntry.getModificationReason())
        );

        verify(auditTimeEntryService).logTimeEntryChange("ADMIN_ADJUST", justification, "{}", "{}", timeEntryId);
    }

    @Test
    void testUpdateTimeEntryThrowsExceptionForShortJustification() {
        UUID timeEntryId = openTimeEntry.getId();
        EditTimeEntryRequestDto dto = new EditTimeEntryRequestDto(OffsetDateTime.now(), null, "Corta");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> timeEntryService.updateTimeEntry(timeEntryId, dto));

        assertEquals("La justificación es obligatoria y debe tener al menos 10 caracteres.", exception.getMessage());
    }

    @Test
    void testVoidTimeEntrySuccess() {
        UUID timeEntryId = openTimeEntry.getId();
        String justification = "Fichaje anulado por error del sistema.";
        VoidTimeEntryRequestDto dto = new VoidTimeEntryRequestDto(justification);

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.of(openTimeEntry));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        timeEntryService.voidTimeEntry(timeEntryId, dto);

        assertAll(
                () -> assertNotNull(openTimeEntry.getDeletedAt()),
                () -> assertEquals(user, openTimeEntry.getDeletedBy()),
                () -> assertEquals(justification, openTimeEntry.getDeleteReason())
        );

        verify(auditTimeEntryService).logTimeEntryChange("SOFT_DELETE", justification, "{}", "{}", timeEntryId);
    }

    @Test
    void testVoidTimeEntryThrowsExceptionForShortJustification() {
        UUID timeEntryId = openTimeEntry.getId();
        VoidTimeEntryRequestDto dto = new VoidTimeEntryRequestDto("Anulado");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> timeEntryService.voidTimeEntry(timeEntryId, dto));

        assertEquals("El motivo de anulación es obligatorio y debe tener al menos 10 caracteres.", exception.getMessage());
    }

    @Test
    void testUpdateTimeEntryNotFound() {
        UUID timeEntryId = UUID.randomUUID();
        EditTimeEntryRequestDto dto = new EditTimeEntryRequestDto(OffsetDateTime.now(), null, "Justificación válida y larga.");

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(timeEntryRepository.findById(timeEntryId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> timeEntryService.updateTimeEntry(timeEntryId, dto));

        assertEquals("No se encontró el fichaje", exception.getMessage());
    }
}
