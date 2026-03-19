package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;

public interface EstadisticaDiariaProjection {
    LocalDate getFecha();

    Long getMinutosTrabajados();
}