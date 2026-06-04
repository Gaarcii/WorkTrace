package com.worktrace.worktracebackend.timeentry.domain.port.out;

import java.time.LocalDate;
import java.util.UUID;

public interface TimeEntryQueryPort {
    Long countByCompanyAndDate(UUID companyId, LocalDate date);

    Long getWorkedMinutesByCompanyAndDate(UUID companyId, LocalDate date);
}
