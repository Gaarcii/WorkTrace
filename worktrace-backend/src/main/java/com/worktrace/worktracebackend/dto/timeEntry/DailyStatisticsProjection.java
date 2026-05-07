package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;

public interface DailyStatisticsProjection {
    LocalDate getFecha();

    Long getMinutosTrabajados();
}