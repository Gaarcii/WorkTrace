package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyStatistic;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyWorkedMinutes;
import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;
import com.worktrace.worktracebackend.timeentry.domain.model.WorkStatistics;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetStatisticsUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso {@link GetStatisticsUseCase}.
 * <p>
 * Resuelve el trabajador autenticado, obtiene su horario semanal y los minutos
 * trabajados por día (agregados en una sola consulta) y recorre el rango día a
 * día combinando ambos para producir el desglose diario y los totales. Rechaza
 * rangos que superen {@link #MAX_RANGE_DAYS}.
 */
public class GetStatisticsUseCaseImpl implements GetStatisticsUseCase {

    /**
     * Máximo rango consultable, en días: los ~4 años de retención legal de
     * fichajes más un pequeño margen. Pasado ese periodo los registros se
     * eliminan de la base de datos.
     */
    private static final long MAX_RANGE_DAYS = 1466;

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;
    private final WorkScheduleQueryPort workScheduleQueryPort;

    public GetStatisticsUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort, WorkScheduleQueryPort workScheduleQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
        this.workScheduleQueryPort = workScheduleQueryPort;
    }

    @Override
    public WorkStatistics execute(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && ChronoUnit.DAYS.between(startDate, endDate) > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("El rango supera el máximo permitido");
        }

        UUID userId = authenticatedUserPort.getAuthenticatedUser().profileUserId();

        Map<DayOfWeek, ScheduledShift> dailyTargetMinutes = workScheduleQueryPort.findByEmployee(userId);

        Map<LocalDate, Long> workedMinutesMap = timeEntryQueryPort.getDailyWorkedMinutesInRange(userId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(DailyWorkedMinutes::date, DailyWorkedMinutes::minutes));

        long totalWorked = 0L;
        long balance = 0L;
        int incompleteWorkdays = 0;
        List<DailyStatistic> dailySummary = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            long plannedMinutes = Optional.ofNullable(dailyTargetMinutes.get(currentDate.getDayOfWeek()))
                    .map(ScheduledShift::durationMinutes)
                    .orElse(0L);
            long workedMinutes = workedMinutesMap.getOrDefault(currentDate, 0L);

            dailySummary.add(new DailyStatistic(currentDate, workedMinutes, plannedMinutes));

            totalWorked += workedMinutes;
            balance += (workedMinutes - plannedMinutes);

            if (plannedMinutes > 0 && workedMinutes < plannedMinutes) {
                incompleteWorkdays++;
            }

            currentDate = currentDate.plusDays(1);
        }
        return new WorkStatistics(totalWorked, balance, incompleteWorkdays, dailySummary);

    }
}
