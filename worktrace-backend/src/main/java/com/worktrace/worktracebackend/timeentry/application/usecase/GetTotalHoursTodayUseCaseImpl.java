package com.worktrace.worktracebackend.timeentry.application.usecase;

import com.worktrace.worktracebackend.shared.port.AuthenticatedUserPort;
import com.worktrace.worktracebackend.timeentry.domain.port.in.GetTotalHoursTodayUseCase;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class GetTotalHoursTodayUseCaseImpl implements GetTotalHoursTodayUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final AuthenticatedUserPort authenticatedUserPort;

    public GetTotalHoursTodayUseCaseImpl(TimeEntryQueryPort timeEntryQueryPort, AuthenticatedUserPort authenticatedUserPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.authenticatedUserPort = authenticatedUserPort;
    }

    @Override
    public Long execute() {
        UUID companyId = authenticatedUserPort.getCompanyId();
        return timeEntryQueryPort.getWorkedMinutesByCompanyAndDate(companyId, LocalDate.now());
    }
}
