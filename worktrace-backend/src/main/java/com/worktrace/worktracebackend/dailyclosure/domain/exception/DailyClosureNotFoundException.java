package com.worktrace.worktracebackend.dailyclosure.domain.exception;

/**
 * Excepción de dominio que indica que no existe ningún cierre diario para la
 * compañía y fecha solicitadas.
 * <p>
 * Se lanza, por ejemplo, al intentar verificar la integridad de un día que aún
 * no ha sido cerrado.
 */
public class DailyClosureNotFoundException extends RuntimeException {

    /**
     * @param date Fecha para la que no se encontró cierre.
     */
    public DailyClosureNotFoundException(java.time.LocalDate date) {
        super("No existe cierre diario para la fecha " + date);
    }
}
