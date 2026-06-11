package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetFirstTimeEntryDateForEmployeeUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetFirstTimeEntryDateForEmployeeUseCase}.
 * <p>
 * Resuelve el usuario autenticado a través del {@link AuthenticatedUserPort} y
 * consulta su primer fichaje mediante el {@link TimeEntryQueryPort}, aplicando
 * un valor por defecto (la fecha actual) cuando el empleado todavía no tiene
 * fichajes.
 */
public class GetFirstTimeEntryDateForEmployeeUseCaseImpl implements GetFirstTimeEntryDateForEmployeeUseCase {
    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetFirstTimeEntryDateForEmployeeUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtiene el identificador del usuario autenticado y devuelve la fecha de su
     * primer fichaje; si no existe ninguno, devuelve la fecha actual.
     */
    @Override
    public LocalDate execute() {
        UUID userId = authenticatedUserPort.getAuthenticatedUser().profileUserId();
        LocalDate firstDate = timeEntryQueryPort.findFirstWorkDateByEmployee(userId);
        return firstDate != null ? firstDate : LocalDate.now();
    }
}
