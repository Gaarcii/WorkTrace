package com.worktrace.worktracebackend.service.auditTimeEntry;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditTimeEntryServiceTest {

    @Mock
    private AuditTimeEntryRepository auditRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuditTimeEntryService auditTimeEntryService;

    private User testUser;
    private Company testCompany;
    private UserAndCompanyInfo testUserAndCompanyInfo;
    private UUID timeEntryId;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(UUID.randomUUID());
        testCompany.setCompanyName("Empresa de Prueba");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("usuario@prueba.com");
        testUser.setCompany(testCompany);

        Profile testProfile = new Profile();
        testProfile.setUserId(testUser.getId());
        testProfile.setFullName("Usuario de Prueba");
        testProfile.setUser(testUser);

        testUserAndCompanyInfo = new UserAndCompanyInfo(testUser, testCompany, testProfile);

        timeEntryId = UUID.randomUUID();
    }

    @Test
    void testLogTimeEntryChangeSuccess() {
        String action = "UPDATE";
        String reason = "Corrección de la hora de finalización";
        String oldDataJson = "{\"endTime\":\"17:00\"}";
        String newDataJson = "{\"endTime\":\"18:00\"}";

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(testUserAndCompanyInfo);

        auditTimeEntryService.logTimeEntryChange(action, reason, oldDataJson, newDataJson, timeEntryId);

        ArgumentCaptor<AuditTimeEntry> auditEntryCaptor = ArgumentCaptor.forClass(AuditTimeEntry.class);
        verify(auditRepository).save(auditEntryCaptor.capture());

        AuditTimeEntry capturedAuditEntry = auditEntryCaptor.getValue();
        assertAll(
                () -> assertNotNull(capturedAuditEntry, "La entrada de auditoría no debería ser nula."),
                () -> assertEquals(timeEntryId, capturedAuditEntry.getTimeEntryId(), "El ID del fichaje no coincide."),
                () -> assertEquals(action, capturedAuditEntry.getAction(), "La acción realizada no coincide."),
                () -> assertEquals(testUser.getId(), capturedAuditEntry.getActorUserId(), "El ID del actor no coincide."),
                () -> assertEquals(reason, capturedAuditEntry.getReason(), "La razón del cambio no coincide."),
                () -> assertEquals(oldDataJson, capturedAuditEntry.getOldData(), "Los datos antiguos no coinciden."),
                () -> assertEquals(newDataJson, capturedAuditEntry.getNewData(), "Los datos nuevos no coinciden."),
                () -> assertEquals(testCompany.getId(), capturedAuditEntry.getCompanyId(), "El ID de la compañía no coincide.")
        );
    }

    @Test
    void testLogTimeEntryChangeThrowsExceptionWhenUserNotFound() {
        String action = "VOID";
        String reason = "Fichaje creado por error";
        String oldDataJson = "{\"startTime\":\"09:00\"}";
        String newDataJson = "{\"startTime\":\"08:00\"}";

        when(userService.getAuthenticatedUserAndCompanyInfo()).thenThrow(new RuntimeException("Usuario no autenticado"));

        assertThrows(RuntimeException.class, () -> auditTimeEntryService.logTimeEntryChange(action, reason, oldDataJson, newDataJson, timeEntryId), "Se esperaba que se lanzara una RuntimeException.");

        verify(auditRepository, never()).save(any(AuditTimeEntry.class));
    }
}


