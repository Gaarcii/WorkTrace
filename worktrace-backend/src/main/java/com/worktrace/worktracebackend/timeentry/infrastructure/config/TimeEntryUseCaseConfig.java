package com.worktrace.worktracebackend.timeentry.infrastructure.config;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetActiveWorkersUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetDailySummaryUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetFirstTimeEntryDateForEmployeeUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetTimeEntryCountTodayUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetTotalHoursTodayUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.GetWeeklyTimeEntryCountChartDataUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.application.usecase.ResolveEmployeeReportRangeUseCaseImpl;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetActiveWorkersUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetDailySummaryUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetFirstTimeEntryDateForEmployeeUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTimeEntryCountTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTotalHoursTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetWeeklyTimeEntryCountChartDataUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.in.ResolveEmployeeReportRangeUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.WorkScheduleQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Cableado de los casos de uso de fichajes.
 * <p>
 * Mantiene la capa de aplicación como POJOs (sin anotaciones de Spring) y declara
 * aquí, en infraestructura, cómo construir cada caso de uso a partir de sus
 * puertos. Spring inyecta los adaptadores ({@link TimeEntryQueryPort},
 * {@link WorkScheduleQueryPort}, {@link AuthenticatedUserPort}) y el
 * {@link ZoneId} de la aplicación como parámetros de cada bean.
 */
@Configuration
public class TimeEntryUseCaseConfig {

    @Bean
    public GetTimeEntryCountTodayUseCase getTimeEntryCountTodayUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        return new GetTimeEntryCountTodayUseCaseImpl(timeEntryQueryPort, authenticatedUserPort);
    }

    @Bean
    public GetTotalHoursTodayUseCase getTotalHoursTodayUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        return new GetTotalHoursTodayUseCaseImpl(timeEntryQueryPort, authenticatedUserPort);
    }

    @Bean
    public GetFirstTimeEntryDateForEmployeeUseCase getFirstTimeEntryDateForEmployeeUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        return new GetFirstTimeEntryDateForEmployeeUseCaseImpl(timeEntryQueryPort, authenticatedUserPort);
    }

    @Bean
    public GetWeeklyTimeEntryCountChartDataUseCase getWeeklyTimeEntryCountChartDataUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        return new GetWeeklyTimeEntryCountChartDataUseCaseImpl(timeEntryQueryPort, authenticatedUserPort);
    }

    @Bean
    public GetActiveWorkersUseCase getActiveWorkersUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort,
            WorkScheduleQueryPort workScheduleQueryPort, ZoneId applicationZoneId) {
        return new GetActiveWorkersUseCaseImpl(
                timeEntryQueryPort, authenticatedUserPort, workScheduleQueryPort, applicationZoneId);
    }

    @Bean
    public GetDailySummaryUseCase getDailySummaryUseCase(
            TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort,
            WorkScheduleQueryPort workScheduleQueryPort) {
        return new GetDailySummaryUseCaseImpl(timeEntryQueryPort, authenticatedUserPort, workScheduleQueryPort);
    }

    @Bean
    public ResolveEmployeeReportRangeUseCase resolveEmployeeReportRangeUseCase(
            GetFirstTimeEntryDateForEmployeeUseCase getFirstTimeEntryDateForEmployeeUseCase) {
        return new ResolveEmployeeReportRangeUseCaseImpl(getFirstTimeEntryDateForEmployeeUseCase);
    }
}
