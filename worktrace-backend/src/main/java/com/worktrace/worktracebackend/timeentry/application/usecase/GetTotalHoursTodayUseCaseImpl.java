package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTotalHoursTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetTotalHoursTodayUseCase}.
 * <p>
 * Resuelve la compañía del usuario autenticado a través del
 * {@link AuthenticatedUserPort} y delega el cálculo de los minutos trabajados
 * hoy en el {@link TimeEntryQueryPort}, garantizando el aislamiento
 * multi-tenant.
 */
public class GetTotalHoursTodayUseCaseImpl implements GetTotalHoursTodayUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetTotalHoursTodayUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Suma los minutos trabajados por la compañía del usuario autenticado para
     * la fecha de hoy.
     */
    @Override
    public Long execute() {
        UUID companyId = authenticatedUserPort.getAuthenticatedUser().companyId();
        return timeEntryQueryPort.getWorkedMinutesByCompanyAndDate(companyId, LocalDate.now());
    }
}
