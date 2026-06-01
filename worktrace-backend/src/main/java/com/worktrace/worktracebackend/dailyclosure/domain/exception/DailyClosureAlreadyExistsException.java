package com.worktrace.worktracebackend.dailyclosure.domain.exception;

public class DailyClosureAlreadyExistsException extends RuntimeException {
    public DailyClosureAlreadyExistsException(java.time.LocalDate date) {
        super("El cierre diario para la fecha " + date + " ya está realizado");
    }
}
