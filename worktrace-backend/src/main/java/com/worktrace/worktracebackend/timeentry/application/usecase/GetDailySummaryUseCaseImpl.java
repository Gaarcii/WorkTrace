package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.ActiveTimeEntry;
import com.worktrace.worktracebackend.timeentry.domain.model.DailySummary;
import com.worktrace.worktracebackend.timeentry.domain.model.LastTimeEntries;
import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetDailySummaryUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetDailySummaryUseCase}.
 * <p>
 * Construye el resumen diario del trabajador autenticado combinando varias
 * consultas de solo lectura: sus últimos eventos de fichaje, su jornada en curso
 * (si la hay), los minutos ya trabajados hoy y su horario previsto para el día.
 * A partir del horario calcula el objetivo de jornada en minutos.
 */
@Service
public class GetDailySummaryUseCaseImpl implements GetDailySummaryUseCase {


    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;
    private final WorkScheduleQueryPort workScheduleQueryPort;

    public GetDailySummaryUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort, WorkScheduleQueryPort workScheduleQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
        this.workScheduleQueryPort = workScheduleQueryPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resuelve el trabajador autenticado, recupera sus últimos eventos de fichaje
     * y delega en {@link #calculateDailySummary(UUID, List)} el cálculo del resto
     * de indicadores del día.
     */
    @Override
    public DailySummary execute() {
        UUID userId = authenticatedUserPort.getAuthenticatedUser().profileUserId();

        List<LastTimeEntries> lastTimeEntries = timeEntryQueryPort.findTop5ByEmployee(userId);

        return calculateDailySummary(userId, lastTimeEntries);
    }

    /**
     * Calcula el resumen diario combinando los datos del trabajador.
     * <p>
     * Obtiene la jornada abierta (para la hora de entrada), los minutos
     * acumulados hoy y el horario del día. El objetivo de jornada se deriva de la
     * duración entre la hora de inicio y fin del horario; si el horario cruza la
     * medianoche (fin anterior al inicio) se le suma un día, y si no hay horario
     * el objetivo es 0.
     *
     * @param userId          Identificador del trabajador.
     * @param lastTimeEntries Últimos eventos de fichaje ya recuperados.
     * @return El {@link DailySummary} resultante.
     */
    private DailySummary calculateDailySummary(UUID userId, List<LastTimeEntries> lastTimeEntries) {

        Optional<ActiveTimeEntry> activeTimeEntryOpt = timeEntryQueryPort.findOpenByEmployee(userId);

        long accumulatedMinutes = timeEntryQueryPort.getWorkedMinutesByEmployeeAndDate(userId, LocalDate.now());

        Optional<ScheduledShift> scheduleOpt = workScheduleQueryPort.findByEmployeeAndDayOfWeek(userId, LocalDate.now().getDayOfWeek());

        Duration targetDuration;
        if (scheduleOpt.isPresent()) {
            LocalTime start = scheduleOpt.get().startTime();
            LocalTime end = scheduleOpt.get().endTime();

            targetDuration = Duration.between(start, end);

            if (targetDuration.isNegative()) {
                targetDuration = targetDuration.plusDays(1);
            }
        } else {
            targetDuration = Duration.ofMinutes(0);
        }

        return new DailySummary(
                accumulatedMinutes,
                targetDuration.toMinutes(),
                activeTimeEntryOpt.map(ActiveTimeEntry::startAt).orElse(null),
                lastTimeEntries
        );
    }
}
