package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.ActiveTimeEntry;
import com.worktrace.worktracebackend.timeentry.domain.model.ActiveWorker;
import com.worktrace.worktracebackend.timeentry.domain.model.ScheduledShift;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetActiveWorkersUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link GetActiveWorkersUseCase}.
 * <p>
 * Combina dos fuentes para construir la vista de trabajadores activos:
 * <ol>
 *   <li>Los fichajes abiertos de la empresa ({@link TimeEntryQueryPort}).</li>
 *   <li>Los horarios previstos de esos empleados para el día de la semana actual
 *       ({@link WorkScheduleQueryPort}).</li>
 * </ol>
 * Para cada fichaje activo calcula la <strong>puntualidad</strong> como la
 * diferencia en minutos entre la hora de entrada real y la prevista en el
 * horario; la hora real se convierte previamente a la zona horaria de la
 * aplicación ({@link ZoneId}) para comparar horas locales coherentes. Si el
 * empleado no tiene horario ese día, la puntualidad queda a {@code null}. La
 * compañía se resuelve desde el usuario autenticado, asegurando el aislamiento
 * multi-tenant.
 */
public class GetActiveWorkersUseCaseImpl implements GetActiveWorkersUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;
    private final WorkScheduleQueryPort workScheduleQueryPort;
    private final ZoneId zoneId;

    public GetActiveWorkersUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort, WorkScheduleQueryPort workScheduleQueryPort, ZoneId zoneId) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
        this.workScheduleQueryPort = workScheduleQueryPort;
        this.zoneId = zoneId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera los fichajes abiertos de la empresa autenticada; si no hay
     * ninguno, devuelve una lista vacía sin consultar horarios. En caso
     * contrario, obtiene en una sola consulta los horarios de todos los
     * empleados activos para el día de la semana actual (indexados por empleado)
     * y los cruza con cada fichaje para calcular su puntualidad.
     */
    @Override
    public List<ActiveWorker> execute() {
        UUID companyId = authenticatedUserPort.getAuthenticatedUser().companyId();

        List<ActiveTimeEntry> timeEntryList = timeEntryQueryPort.findActiveByCompany(companyId);

        if (timeEntryList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> employeesIds = timeEntryList.stream()
                .map(ActiveTimeEntry::employeeId)
                .toList();

        Map<UUID, ScheduledShift> scheduledShiftsList = workScheduleQueryPort
                .findByEmployeesAndDayOfWeek(employeesIds, LocalDate.now().getDayOfWeek());


        return timeEntryList.stream()
                .map(activeTimeEntry -> {
                    UUID employeeId = activeTimeEntry.employeeId();
                    String fullName = activeTimeEntry.fullName();
                    String jobPosition = activeTimeEntry.jobPosition();
                    String avatarUrl = activeTimeEntry.avatarUrl();
                    OffsetDateTime entryTime = activeTimeEntry.startAt();

                    ScheduledShift shift = scheduledShiftsList.get(employeeId);
                    Long punctualityMinutes = null;

                    if (shift != null) {
                        LocalTime scheduleTime = shift.startTime();
                        LocalTime localTimeEntry = activeTimeEntry.startAt().atZoneSameInstant(zoneId).toLocalTime();
                        punctualityMinutes = Duration.between(scheduleTime, localTimeEntry).toMinutes();
                    }
                    return new ActiveWorker(
                            employeeId, fullName, jobPosition, avatarUrl, entryTime, punctualityMinutes);

                }).toList();
    }
}
