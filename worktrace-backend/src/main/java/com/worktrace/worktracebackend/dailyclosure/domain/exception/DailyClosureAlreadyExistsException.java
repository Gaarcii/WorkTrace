package com.worktrace.worktracebackend.dailyclosure.domain.exception;

/**
 * Excepción de dominio que indica que ya existe un cierre diario para la
 * compañía y fecha indicadas.
 * <p>
 * Protege la regla de negocio que prohíbe cerrar dos veces el mismo día,
 * evitando registros de cierre duplicados.
 */
public class DailyClosureAlreadyExistsException extends RuntimeException {

    /**
     * @param date Fecha cuyo cierre ya estaba realizado.
     */
    public DailyClosureAlreadyExistsException(java.time.LocalDate date) {
        super("El cierre diario para la fecha " + date + " ya está realizado");
    }
}
