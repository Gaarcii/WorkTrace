package com.worktrace.worktracebackend.dailyclosure.domain.exception;

public class OpenShiftsExistException extends RuntimeException {
    public OpenShiftsExistException(long count) {
        super("Hay " + count + " turnos abiertos, no se puede cerrar el día");
    }
}
