package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;

public interface DailyFichajeCountProjection {
    LocalDate getFecha();

    Long getNumFichajes();
}
