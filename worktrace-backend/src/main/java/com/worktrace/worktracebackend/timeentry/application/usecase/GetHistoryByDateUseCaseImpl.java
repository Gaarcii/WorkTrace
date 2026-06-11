package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyHistory;
import com.worktrace.worktracebackend.timeentry.domain.model.LastTimeEntries;
import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetHistoryByDateUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.ProfileQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetHistoryByDateUseCase}.
 * <p>
 * Es un POJO sin anotaciones de Spring; su cableado se declara explícitamente en
 * {@code TimeEntryUseCaseConfig}. Construye el historial del día combinando
 * cuatro puertos de salida: los fichajes ({@link TimeEntryQueryPort}), el horario
 * ({@link WorkScheduleQueryPort}), el perfil ({@link ProfileQueryPort}) y el
 * usuario autenticado ({@link AuthenticatedUserPort}).
 * <p>
 * Calcula cuatro indicadores: minutos trabajados y objetivo del día, y minutos
 * trabajados y objetivo de la semana (lunes-domingo de la fecha). El objetivo
 * semanal se deriva de las horas contratadas del perfil (horas × 60).
 */
public class GetHistoryByDateUseCaseImpl implements GetHistoryByDateUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;
    private final WorkScheduleQueryPort workScheduleQueryPort;
    private final ProfileQueryPort profileQueryPort;

    public GetHistoryByDateUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort, WorkScheduleQueryPort workScheduleQueryPort, ProfileQueryPort profileQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
        this.workScheduleQueryPort = workScheduleQueryPort;
        this.profileQueryPort = profileQueryPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resuelve el trabajador autenticado y, para la fecha dada, obtiene: los
     * minutos trabajados y el objetivo del día (duración del turno del horario);
     * los eventos del día; y los totales de la semana que contiene la fecha,
     * derivando el objetivo semanal de las horas contratadas del perfil. Los
     * indicadores sin dato disponible se devuelven como 0.
     */
    @Override
    public DailyHistory execute(LocalDate date) {
        UUID userId = authenticatedUserPort.getAuthenticatedUser().profileUserId();

        long dailyWorkedMinutes = timeEntryQueryPort.getWorkedMinutesByEmployeeAndDate(userId, date);

        long dailyTargetMinutes = workScheduleQueryPort
                .findByEmployeeAndDayOfWeek(userId, date.getDayOfWeek())
                .map(ScheduledShift::durationMinutes)
                .orElse(0L);

        List<LastTimeEntries> dailyRecords = timeEntryQueryPort.findEventsByEmployeeAndDate(userId, date);

        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);

        long weeklyWorkedMinutes = timeEntryQueryPort.getWorkedMinutesByEmployeeAndDateRange(userId, monday, sunday);

        long weeklyTargetMinutes = profileQueryPort.findWeeklyHoursByEmployee(userId)
                .map(h -> h.multiply(BigDecimal.valueOf(60)).longValue())
                .orElse(0L);

        return new DailyHistory(
                dailyWorkedMinutes,
                dailyTargetMinutes,
                weeklyWorkedMinutes,
                weeklyTargetMinutes,
                dailyRecords
        );
    }
}
