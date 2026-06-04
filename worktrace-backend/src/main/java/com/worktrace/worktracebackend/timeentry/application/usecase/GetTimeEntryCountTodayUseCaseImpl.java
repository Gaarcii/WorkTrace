package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTimeEntryCountTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetTimeEntryCountTodayUseCase}.
 * <p>
 * Resuelve la compañía del usuario autenticado a través del
 * {@link AuthenticatedUserPort} y delega el recuento de fichajes de hoy en el
 * {@link TimeEntryQueryPort}, garantizando el aislamiento multi-tenant.
 */
@Service
public class GetTimeEntryCountTodayUseCaseImpl implements GetTimeEntryCountTodayUseCase {
    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetTimeEntryCountTodayUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Cuenta los fichajes de la compañía del usuario autenticado para la fecha
     * de hoy.
     */
    @Override
    public Long execute() {
        UUID companyId= authenticatedUserPort.getAuthenticatedUser().companyId();
        return timeEntryQueryPort.countByCompanyAndDate(companyId, LocalDate.now());
    }
}
