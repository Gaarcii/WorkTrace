package com.worktrace.worktracebackend.dailyclosure.domain.exception;

/**
 * Excepción de dominio que indica que todavía existen turnos (fichajes) abiertos
 * y, por tanto, el día no puede cerrarse.
 * <p>
 * Protege la regla de negocio que exige que todas las jornadas estén finalizadas
 * antes de consolidar el cierre diario y calcular su hash de integridad.
 */
public class OpenShiftsExistException extends RuntimeException {

    /**
     * @param count Número de turnos abiertos que impiden el cierre.
     */
    public OpenShiftsExistException(long count) {
        super("Hay " + count + " turnos abiertos, no se puede cerrar el día");
    }
}
