package com.worktrace.worktracebackend.dailyclosure.domain.exception;

public class DailyClosureNotFoundException extends RuntimeException {
    public DailyClosureNotFoundException(java.time.LocalDate date) {
        super("No existe cierre diario para la fecha " + date);
    }
}
