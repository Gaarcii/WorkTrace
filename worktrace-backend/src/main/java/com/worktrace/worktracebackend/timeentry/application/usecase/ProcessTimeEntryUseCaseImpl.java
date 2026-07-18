package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.model.AuthenticatedUser;
import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventCommand;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventRequest;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventResult;
import com.worktrace.worktracebackend.timeentry.domain.model.IpAnalysis;
import com.worktrace.worktracebackend.timeentry.domain.port.in.ProcessTimeEntryUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.IpAnalysisPort;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryCommandPort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso {@link ProcessTimeEntryUseCase}.
 * <p>
 * Orquesta el registro de un fichaje: resuelve la empresa y el trabajador desde
 * el usuario autenticado, analiza la IP de origen ({@link IpAnalysisPort}) y
 * compone las banderas de auditoría combinando la precisión del GPS con las
 * detectadas en la IP. Con esos datos construye la orden que el puerto de
 * escritura ({@link TimeEntryCommandPort}) persiste de forma atómica, abriendo o
 * cerrando la jornada según el estado del trabajador.
 */
public class ProcessTimeEntryUseCaseImpl implements ProcessTimeEntryUseCase {

    private static final int LOW_GPS_ACCURACY_THRESHOLD_METERS = 200;
    private static final String LOW_GPS_ACCURACY_FLAG = "LOW_GPS_ACCURACY";

    private final AuthenticatedUserPort authenticatedUserPort;
    private final IpAnalysisPort ipAnalysisPort;
    private final TimeEntryCommandPort timeEntryCommandPort;

    public ProcessTimeEntryUseCaseImpl(AuthenticatedUserPort authenticatedUserPort,
                                       IpAnalysisPort ipAnalysisPort,
                                       TimeEntryCommandPort timeEntryCommandPort) {
        this.authenticatedUserPort = authenticatedUserPort;
        this.ipAnalysisPort = ipAnalysisPort;
        this.timeEntryCommandPort = timeEntryCommandPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Analiza la IP de origen, marca la baja precisión del GPS (más de
     * {@value #LOW_GPS_ACCURACY_THRESHOLD_METERS} metros) y añade las banderas
     * del análisis de IP. Delega la apertura o el cierre de la jornada en el
     * puerto de escritura, que lo resuelve atómicamente.
     */
    @Override
    public ClockEventResult execute(ClockEventRequest request) {
        AuthenticatedUser authenticatedUser = authenticatedUserPort.getAuthenticatedUser();
        UUID companyId = authenticatedUser.companyId();
        UUID employeeUserId = authenticatedUser.profileUserId();

        IpAnalysis ipAnalysis = ipAnalysisPort.analyze(request.realIp(), companyId);

        List<String> flags = new ArrayList<>();
        if (request.accuracyMeters() != null && request.accuracyMeters() > LOW_GPS_ACCURACY_THRESHOLD_METERS) {
            flags.add(LOW_GPS_ACCURACY_FLAG);
        }
        if (ipAnalysis.flags() != null) {
            flags.addAll(ipAnalysis.flags());
        }

        ClockEventCommand command = new ClockEventCommand(
                companyId,
                employeeUserId,
                request.lat(),
                request.lng(),
                request.accuracyMeters(),
                request.realIp(),
                request.userAgent(),
                ipAnalysis.geoIpMap(),
                flags
        );

        return timeEntryCommandPort.registerClockEvent(command);
    }
}
