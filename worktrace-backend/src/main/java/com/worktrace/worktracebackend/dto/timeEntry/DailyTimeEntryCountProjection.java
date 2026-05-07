package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;

public interface DailyTimeEntryCountProjection {
    LocalDate getFecha();
    Long getNumFichajes();
}
