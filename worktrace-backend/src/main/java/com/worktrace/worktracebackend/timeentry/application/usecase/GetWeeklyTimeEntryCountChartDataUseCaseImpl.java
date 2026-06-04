package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyEntryCount;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetWeeklyTimeEntryCountChartDataUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso {@link GetWeeklyTimeEntryCountChartDataUseCase}.
 * <p>
 * Resuelve la compañía del usuario autenticado a través del
 * {@link AuthenticatedUserPort} y consulta los recuentos por día mediante el
 * {@link TimeEntryQueryPort}. Como la consulta solo devuelve los días que tienen
 * fichajes, este caso de uso <strong>rellena los días sin actividad</strong> con
 * recuento {@code 0} para entregar una serie continua y sin huecos al gráfico.
 */
@Service
public class GetWeeklyTimeEntryCountChartDataUseCaseImpl implements GetWeeklyTimeEntryCountChartDataUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetWeeklyTimeEntryCountChartDataUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Obtiene los recuentos de la compañía autenticada en el rango, los indexa
     * por fecha y recorre día a día desde {@code startDate} hasta {@code endDate}
     * (ambos incluidos), añadiendo {@code 0} en los días sin fichajes.
     */
    @Override
    public List<DailyEntryCount> execute(LocalDate startDate, LocalDate endDate) {
        UUID companyId = authenticatedUserPort.getAuthenticatedUser().companyId();

        List<DailyEntryCount> timeEntryCounts = timeEntryQueryPort.countByCompanyAndDateRange(companyId, startDate, endDate);

        Map<LocalDate, Long> countByDateMap = timeEntryCounts.stream()
                .collect(Collectors.toMap(DailyEntryCount::date, DailyEntryCount::count));

        List<DailyEntryCount> result = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            Long timeEntryCount = countByDateMap.getOrDefault(date, 0L);
            result.add(new DailyEntryCount(date, timeEntryCount));
            date = date.plusDays(1);
        }
        return result;
    }
}
