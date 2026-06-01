package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyIntegrityUseCaseImplTest {

    @Mock private DailyClosurePort dailyClosurePort;
    @Mock private TimeEntryQueryPort timeEntryQueryPort;
    @Mock private AuditQueryPort auditQueryPort;
    @Mock private HashPort hashPort;

    private VerifyIntegrityUseCaseImpl useCase;

    private UUID companyId;
    private LocalDate date;
    private OffsetDateTime computedAt;

    @BeforeEach
    void setUp() {
        useCase = new VerifyIntegrityUseCaseImpl(timeEntryQueryPort, dailyClosurePort, hashPort, auditQueryPort);
        companyId = UUID.randomUUID();
        date = LocalDate.now().minusDays(1);
        computedAt = OffsetDateTime.now().minusHours(1);
    }

    @Test
    void execute_hashCoincide_devuelveValid() {
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, "V2:abc123", "prevHash", 0, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("abc123");

        IntegrityResult result = useCase.execute(companyId, date);

        assertEquals(IntegrityResult.VALID, result);
    }

    @Test
    void execute_hashDistintoConAuditorias_devuelveModified() {
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, "V2:stored_hash", "prevHash", 0, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("different_hash");
        when(auditQueryPort.hasEditsAfterClosure(companyId, date, computedAt)).thenReturn(true);

        IntegrityResult result = useCase.execute(companyId, date);

        assertEquals(IntegrityResult.MODIFIED, result);
    }

    @Test
    void execute_hashDistintoSinAuditorias_devuelveCorrupted() {
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, "V2:stored_hash", "prevHash", 0, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("different_hash");
        when(auditQueryPort.hasEditsAfterClosure(companyId, date, computedAt)).thenReturn(false);

        IntegrityResult result = useCase.execute(companyId, date);

        assertEquals(IntegrityResult.CORRUPTED, result);
    }

    @Test
    void execute_sinCierre_lanzaDailyClosureNotFoundException() {
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.empty());

        assertThrows(DailyClosureNotFoundException.class, () -> useCase.execute(companyId, date));
    }
}
