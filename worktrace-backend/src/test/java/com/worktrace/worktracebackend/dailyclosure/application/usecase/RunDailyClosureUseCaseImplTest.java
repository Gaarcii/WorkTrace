package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunDailyClosureUseCaseImplTest {

    @Mock private CompanyQueryPort companyQueryPort;
    @Mock private TimeEntryQueryPort timeEntryQueryPort;
    @Mock private DailyClosurePort dailyClosurePort;
    @Mock private HashPort hashPort;

    private RunDailyClosureUseCaseImpl useCase;

    private UUID companyId;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        useCase = new RunDailyClosureUseCaseImpl(companyQueryPort, timeEntryQueryPort, dailyClosurePort, hashPort);
        companyId = UUID.randomUUID();
        targetDate = LocalDate.now().minusDays(1);
    }

    @Test
    void execute_cierreExitoso_guardaElRegistro() {
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId));
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId, targetDate)).thenReturn(0L);
        when(dailyClosurePort.findPreviousHash(companyId, targetDate)).thenReturn(Optional.empty());
        when(timeEntryQueryPort.findOrderedForClosure(companyId, targetDate)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("abc123");

        useCase.execute(targetDate);

        ArgumentCaptor<DailyClosureRecord> captor = ArgumentCaptor.forClass(DailyClosureRecord.class);
        verify(dailyClosurePort).save(captor.capture());
        DailyClosureRecord saved = captor.getValue();
        assertEquals(companyId, saved.companyId());
        assertEquals(targetDate, saved.workDate());
        assertEquals("V2:abc123", saved.dayHash());
        assertEquals(0, saved.recordsCount());
    }

    @Test
    void execute_cierreYaExiste_noGuardaYContinua() {
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId));
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(true);

        useCase.execute(targetDate);

        verify(dailyClosurePort, never()).save(any());
    }

    @Test
    void execute_turnosAbiertos_noGuardaYContinua() {
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId));
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId, targetDate)).thenReturn(3L);

        useCase.execute(targetDate);

        verify(dailyClosurePort, never()).save(any());
    }

    @Test
    void execute_falloEnUnaEmpresa_procesaElResto() {
        UUID companyId2 = UUID.randomUUID();
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId, companyId2));
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(true);
        when(dailyClosurePort.existsForDate(companyId2, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId2, targetDate)).thenReturn(0L);
        when(dailyClosurePort.findPreviousHash(companyId2, targetDate)).thenReturn(Optional.empty());
        when(timeEntryQueryPort.findOrderedForClosure(companyId2, targetDate)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("xyz789");

        useCase.execute(targetDate);

        verify(dailyClosurePort, times(1)).save(any());
    }

    @Test
    void execute_sinEmpresasPrevias_usaGenesisHash() {
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId));
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId, targetDate)).thenReturn(0L);
        when(dailyClosurePort.findPreviousHash(companyId, targetDate)).thenReturn(Optional.empty());
        when(timeEntryQueryPort.findOrderedForClosure(companyId, targetDate)).thenReturn(List.of());
        when(hashPort.sha256Hex(anyString())).thenReturn("hash");

        useCase.execute(targetDate);

        ArgumentCaptor<DailyClosureRecord> captor = ArgumentCaptor.forClass(DailyClosureRecord.class);
        verify(dailyClosurePort).save(captor.capture());
        assertTrue(captor.getValue().prevDayHash().startsWith("GENESIS_HASH"));
    }
}
