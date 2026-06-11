package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.timeentry.domain.model.DateRange;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetFirstTimeEntryDateForEmployeeUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.ResolveEmployeeReportRangeUseCase;

import java.time.LocalDate;

/**
 * Implementación del caso de uso {@link ResolveEmployeeReportRangeUseCase}.
 * <p>
 * Aplica la regla de defaulting del rango de informes: la fecha de fin por
 * defecto es hoy y la de inicio por defecto es la del primer fichaje del
 * trabajador, que delega en {@link GetFirstTimeEntryDateForEmployeeUseCase} para
 * no duplicar esa lógica.
 */
public class ResolveEmployeeReportRangeUseCaseImpl implements ResolveEmployeeReportRangeUseCase {

    private final GetFirstTimeEntryDateForEmployeeUseCase getFirstTimeEntryDateForEmployeeUseCase;

    public ResolveEmployeeReportRangeUseCaseImpl(GetFirstTimeEntryDateForEmployeeUseCase getFirstTimeEntryDateForEmployeeUseCase) {
        this.getFirstTimeEntryDateForEmployeeUseCase = getFirstTimeEntryDateForEmployeeUseCase;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resuelve primero la fecha de fin (hoy si no se indica) y luego la de inicio
     * (el primer fichaje del trabajador si no se indica).
     */
    @Override
    public DateRange execute(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStart = startDate != null ? startDate : getFirstTimeEntryDateForEmployeeUseCase.execute();
        return new DateRange(resolvedStart, resolvedEnd);
    }
}
