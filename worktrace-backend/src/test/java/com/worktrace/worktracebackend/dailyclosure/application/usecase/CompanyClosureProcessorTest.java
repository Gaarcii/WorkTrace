package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureAlreadyExistsException;
import com.worktrace.worktracebackend.dailyclosure.domain.exception.OpenShiftsExistException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyClosureProcessorTest {

    @Mock private TimeEntryQueryPort timeEntryQueryPort;
    @Mock private DailyClosurePort dailyClosurePort;

    private CompanyClosureProcessor processor;

    private UUID companyId;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        processor = new CompanyClosureProcessor(timeEntryQueryPort, dailyClosurePort);
        companyId = UUID.randomUUID();
        targetDate = LocalDate.now().minusDays(1);
    }

    @Test
    void process_cierreExitoso_guardaElRegistro() {
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId, targetDate)).thenReturn(0L);
        when(dailyClosurePort.findPreviousHash(companyId, targetDate)).thenReturn(Optional.of("prevHash"));
        when(timeEntryQueryPort.findOrderedForClosure(companyId, targetDate)).thenReturn(Stream.empty());

        processor.process(companyId, targetDate);

        ArgumentCaptor<DailyClosureRecord> captor = ArgumentCaptor.forClass(DailyClosureRecord.class);
        verify(dailyClosurePort).save(captor.capture());
        DailyClosureRecord saved = captor.getValue();
        assertEquals(companyId, saved.companyId());
        assertEquals(targetDate, saved.workDate());
        assertTrue(saved.dayHash().startsWith("V2:"));
        assertEquals("prevHash", saved.prevDayHash());
        assertEquals(0, saved.recordsCount());
    }

    @Test
    void process_cierreYaExiste_lanzaExcepcion() {
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(true);

        assertThrows(DailyClosureAlreadyExistsException.class, () -> processor.process(companyId, targetDate));
        verify(dailyClosurePort, never()).save(any());
    }

    @Test
    void process_turnosAbiertos_lanzaExcepcion() {
        when(dailyClosurePort.existsForDate(companyId, targetDate)).thenReturn(false);
        when(timeEntryQueryPort.countOpenShifts(companyId, targetDate)).thenReturn(3L);

        assertThrows(OpenShiftsExistException.class, () -> processor.process(companyId, targetDate));
        verify(dailyClosurePort, never()).save(any());
    }
}