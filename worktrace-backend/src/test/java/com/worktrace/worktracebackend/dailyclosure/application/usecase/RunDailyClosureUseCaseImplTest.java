package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunDailyClosureUseCaseImplTest {

    @Mock private CompanyQueryPort companyQueryPort;
    @Mock private CompanyClosureProcessor companyClosureProcessor;

    private RunDailyClosureUseCaseImpl useCase;

    private UUID companyId;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        useCase = new RunDailyClosureUseCaseImpl(companyQueryPort, companyClosureProcessor, 8);
        companyId = UUID.randomUUID();
        targetDate = LocalDate.now().minusDays(1);
    }

    @Test
    void execute_cierreExitoso_guardaElRegistro() {
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId));

        useCase.execute(targetDate);

        verify(companyClosureProcessor).process(companyId, targetDate);
    }

    @Test
    void execute_falloEnUnaEmpresa_procesaElResto() {
        UUID companyId2 = UUID.randomUUID();
        when(companyQueryPort.findAllCompanyIds()).thenReturn(List.of(companyId, companyId2));
        doThrow(new RuntimeException("Test Exception")).when(companyClosureProcessor).process(companyId, targetDate);

        useCase.execute(targetDate);

        verify(companyClosureProcessor).process(companyId, targetDate);
        verify(companyClosureProcessor).process(companyId2, targetDate);
    }
}