package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;

public interface DailyStatisticsProjection {
    LocalDate getDate();

    Long getWorkedMinutes();
}