package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyIntegrityUseCaseImplTest {

    @Mock private DailyClosurePort dailyClosurePort;
    @Mock private TimeEntryQueryPort timeEntryQueryPort;
    @Mock private AuditQueryPort auditQueryPort;

    private VerifyIntegrityUseCaseImpl useCase;

    private UUID companyId;
    private LocalDate date;
    private OffsetDateTime computedAt;

    private static final DailyHashChain HASH_CHAIN = new DailyHashChain();

    @BeforeEach
    void setUp() {
        useCase = new VerifyIntegrityUseCaseImpl(timeEntryQueryPort, dailyClosurePort, auditQueryPort);
        companyId = UUID.randomUUID();
        date = LocalDate.now().minusDays(1);
        computedAt = OffsetDateTime.now().minusHours(1);
    }

    private String hashOf(String prevHash, TimeEntrySnapshot... snapshots) {
        return HASH_CHAIN.compute(Stream.of(snapshots), prevHash).hash();
    }

    private TimeEntrySnapshot snapshot(UUID id, OffsetDateTime startAt) {
        return new TimeEntrySnapshot(
                id,
                UUID.randomUUID(),
                date,
                startAt,
                startAt.plusHours(8),
                BigDecimal.valueOf(40.416775),
                BigDecimal.valueOf(-3.703790),
                null, null,
                10, null,
                "1.2.3.4", null,
                "Mozilla/5.0", null,
                null, null,
                List.of(),
                TimeEntryStatus.CLOSED,
                null, null, null,
                computedAt.minusHours(9),
                UUID.randomUUID(),
                null, null,
                companyId
        );
    }

    @Test
    void execute_hashCoincide_devuelveValid() {
        String prevHash = "prevHash";
        String expectedHash = "V2:c8b6c189f134376a0947c990cc3fc51414d2f2dd59d1fabfe99123f8c0093398";
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, expectedHash, prevHash, 0, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(Stream.empty());

        assertEquals(IntegrityResult.VALID, useCase.execute(companyId, date));
    }

    @Test
    void execute_hashDistintoSinAuditorias_devuelveCorrupted() {
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, "V2:stored_hash", "prevHash", 0, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(Stream.empty());
        when(auditQueryPort.getChangesAfterClosure(companyId, date, computedAt)).thenReturn(List.of());

        assertEquals(IntegrityResult.CORRUPTED, useCase.execute(companyId, date));
    }

    @Test
    void execute_cambiosExplicadosCompletamentePorAudit_devuelveModified() {
        UUID entryId = UUID.randomUUID();
        TimeEntrySnapshot atClosure = snapshot(entryId, computedAt.minusHours(8));
        TimeEntrySnapshot afterEdit  = snapshot(entryId, computedAt.minusHours(7)); // startAt modificado por app
        String storedHash = hashOf("prevHash", atClosure);

        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, storedHash, "prevHash", 1, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(Stream.of(afterEdit));
        when(auditQueryPort.getChangesAfterClosure(companyId, date, computedAt))
                .thenReturn(List.of(new AuditedChange(entryId, "ADMIN_ADJUST", atClosure)));

        assertEquals(IntegrityResult.MODIFIED, useCase.execute(companyId, date));
    }

    @Test
    void execute_auditNoExplicaHashPorEdicionDirectaEnBD_devuelveTampered() {
        // Escenario: edición directa en BD (no auditada) + edición posterior por app (auditada)
        // El old_data del audit refleja el estado tras la edición en BD, no el estado en el cierre.
        UUID entryId = UUID.randomUUID();
        TimeEntrySnapshot atClosure  = snapshot(entryId, computedAt.minusHours(8)); // estado real en el cierre
        TimeEntrySnapshot dbModified = snapshot(entryId, computedAt.minusHours(7)); // edición directa en BD
        TimeEntrySnapshot afterEdit  = snapshot(entryId, computedAt.minusHours(6)); // edición posterior por app
        String storedHash = hashOf("prevHash", atClosure);

        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, storedHash, "prevHash", 1, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(Stream.of(afterEdit));
        // El audit guarda dbModified como old_data porque la app vio el estado ya modificado por BD
        when(auditQueryPort.getChangesAfterClosure(companyId, date, computedAt))
                .thenReturn(List.of(new AuditedChange(entryId, "ADMIN_ADJUST", dbModified)));

        assertEquals(IntegrityResult.TAMPERED, useCase.execute(companyId, date));
    }

    @Test
    void execute_borradoDirectoEnBDMasAuditLegitimo_devuelveTampered() {
        // Escenario: fila A borrada directamente en BD + fila B editada legítimamente por app
        UUID entryIdA = UUID.randomUUID();
        UUID entryIdB = UUID.randomUUID();
        TimeEntrySnapshot aAtClosure = snapshot(entryIdA, computedAt.minusHours(9));
        TimeEntrySnapshot bAtClosure = snapshot(entryIdB, computedAt.minusHours(8));
        TimeEntrySnapshot bAfterEdit = snapshot(entryIdB, computedAt.minusHours(7));
        String storedHash = hashOf("prevHash", aAtClosure, bAtClosure);

        // A ha desaparecido de la BD (borrado físico), B está modificada por app
        DailyClosureRecord closure = new DailyClosureRecord(companyId, date, storedHash, "prevHash", 2, computedAt);
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.of(closure));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, date)).thenReturn(Stream.of(bAfterEdit));
        when(auditQueryPort.getChangesAfterClosure(companyId, date, computedAt))
                .thenReturn(List.of(new AuditedChange(entryIdB, "ADMIN_ADJUST", bAtClosure)));

        // Reconstrucción: {B: bAtClosure} — A nunca aparece → hash ≠ storedHash
        assertEquals(IntegrityResult.TAMPERED, useCase.execute(companyId, date));
    }

    @Test
    void execute_sinCierre_lanzaDailyClosureNotFoundException() {
        when(dailyClosurePort.findByDate(companyId, date)).thenReturn(Optional.empty());

        assertThrows(DailyClosureNotFoundException.class, () -> useCase.execute(companyId, date));
    }
}
