package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.model.PageResult;
import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.TimeEntryRow;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTimeEntriesByEmployeeUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;

import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetTimeEntriesByEmployeeUseCase}.
 * <p>
 * Resuelve la empresa del usuario autenticado y delega la consulta paginada en el
 * {@link TimeEntryQueryPort}. Antes de consultar, <strong>sanea los parámetros de
 * paginación</strong>: corrige índices de página negativos y acota el tamaño al
 * rango [1, {@link #MAX_PAGE_SIZE}], evitando que un {@code size} desmedido
 * degrade la consulta.
 */
public class GetTimeEntriesByEmployeeUseCaseImpl implements GetTimeEntriesByEmployeeUseCase {

    /** Tamaño máximo de página, para que un {@code size} desmedido no degrade la consulta. */
    private static final int MAX_PAGE_SIZE = 100;

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetTimeEntriesByEmployeeUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sanea {@code page} (mínimo 0) y {@code size} (entre 1 y
     * {@link #MAX_PAGE_SIZE}) y consulta los fichajes del empleado dentro de la
     * empresa del usuario autenticado.
     */
    @Override
    public PageResult<TimeEntryRow> execute(UUID employeeId, int page, int size) {
        UUID companyId = authenticatedUserPort.getAuthenticatedUser().companyId();

        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 1 : Math.min(size, MAX_PAGE_SIZE);

        return timeEntryQueryPort.findByEmployeePaged(companyId, employeeId, safePage, safeSize);
    }
}
