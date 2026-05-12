package com.worktrace.worktracebackend.service.dailyClosure;

import com.worktrace.worktracebackend.model.*;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.DailyClosureRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.service.auth.UserAndCompanyInfo;
import com.worktrace.worktracebackend.service.auth.UserService;
import com.worktrace.worktracebackend.service.hash.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyClosureServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private DailyClosureRepository dailyClosureRepository;
    @Mock
    private HashService hashService;
    @Mock
    private UserService userService;
    @Mock
    private AuditTimeEntryRepository auditTimeEntryRepository;

    @InjectMocks
    private DailyClosureService dailyClosureService;

    private Company testCompany;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(UUID.randomUUID());
        testCompany.setCompanyName("Empresa de Prueba");

        targetDate = LocalDate.now().minusDays(1);
    }

    @Test
    void testRunDailyClosureSuccess() {
        when(companyRepository.findAll()).thenReturn(List.of(testCompany));
        when(dailyClosureRepository.existsByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(false);
        when(timeEntryRepository.countByCompanyIdAndWorkDateAndTimeEntryStatus(testCompany.getId(), targetDate, TimeEntryStatus.OPEN)).thenReturn(0L);
        when(dailyClosureRepository.findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(testCompany.getId(), targetDate)).thenReturn(Optional.empty());
        when(timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(testCompany.getId(), targetDate)).thenReturn(Collections.emptyList());
        when(hashService.sha256Hex(anyString())).thenReturn("new_hash");

        dailyClosureService.runDailyClosure();

        verify(dailyClosureRepository).save(any(DailyClosure.class));
    }

    @Test
    void testRunDailyClosureError_ClosureAlreadyExists() {
        when(companyRepository.findAll()).thenReturn(List.of(testCompany));
        when(dailyClosureRepository.existsByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(true);

        dailyClosureService.runDailyClosure();

        verify(dailyClosureRepository, never()).save(any(DailyClosure.class));
    }

    @Test
    void testRunDailyClosureError_OpenShifts() {
        when(companyRepository.findAll()).thenReturn(List.of(testCompany));
        when(dailyClosureRepository.existsByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(false);
        when(timeEntryRepository.countByCompanyIdAndWorkDateAndTimeEntryStatus(testCompany.getId(), targetDate, TimeEntryStatus.OPEN)).thenReturn(1L);

        dailyClosureService.runDailyClosure();

        verify(dailyClosureRepository, never()).save(any(DailyClosure.class));
    }

    @Test
    void testVerifyIntegrityValid() {
        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(new User(), testCompany, new Profile());
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        DailyClosure closure = new DailyClosure();
        closure.setDayHash("valid_hash");
        closure.setPrevDayHash("previous_hash");
        when(dailyClosureRepository.findByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(Optional.of(closure));

        when(timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(testCompany.getId(), targetDate)).thenReturn(Collections.emptyList());
        when(hashService.sha256Hex(anyString())).thenReturn("valid_hash");

        String result = dailyClosureService.verifyIntegrity(targetDate);

        assertEquals("VALID", result);
    }

    @Test
    void testVerifyIntegrityModified() {
        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(new User(), testCompany, new Profile());
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        DailyClosure closure = new DailyClosure();
        closure.setDayHash("stored_hash");
        closure.setPrevDayHash("previous_hash");
        closure.setComputedAt(OffsetDateTime.now().minusHours(1));
        when(dailyClosureRepository.findByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(Optional.of(closure));

        when(timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(testCompany.getId(), targetDate)).thenReturn(Collections.emptyList());
        when(hashService.sha256Hex(anyString())).thenReturn("different_hash");
        when(auditTimeEntryRepository.existsEditsAfterClosure(testCompany.getId(), targetDate, closure.getComputedAt())).thenReturn(true);

        String result = dailyClosureService.verifyIntegrity(targetDate);

        assertEquals("MODIFIED", result);
    }

    @Test
    void testVerifyIntegrityCorrupted() {
        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(new User(), testCompany, new Profile());
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);

        DailyClosure closure = new DailyClosure();
        closure.setDayHash("stored_hash");
        closure.setPrevDayHash("previous_hash");
        closure.setComputedAt(OffsetDateTime.now().minusHours(1));
        when(dailyClosureRepository.findByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(Optional.of(closure));

        when(timeEntryRepository.findByCompanyIdAndWorkDateOrderByStartAtAscIdAsc(testCompany.getId(), targetDate)).thenReturn(Collections.emptyList());
        when(hashService.sha256Hex(anyString())).thenReturn("different_hash");
        when(auditTimeEntryRepository.existsEditsAfterClosure(testCompany.getId(), targetDate, closure.getComputedAt())).thenReturn(false);

        String result = dailyClosureService.verifyIntegrity(targetDate);

        assertEquals("CORRUPTED", result);
    }

    @Test
    void testVerifyIntegrityError_NoClosureFound() {
        UserAndCompanyInfo userAndCompanyInfo = new UserAndCompanyInfo(new User(), testCompany, new Profile());
        when(userService.getAuthenticatedUserAndCompanyInfo()).thenReturn(userAndCompanyInfo);
        when(dailyClosureRepository.findByCompanyIdAndWorkDate(testCompany.getId(), targetDate)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> dailyClosureService.verifyIntegrity(targetDate));

        assertEquals("No hay cierre para esta fecha", exception.getMessage());
    }
}
