package com.worktrace.worktracebackend.timeentry.domain.port.in;

/**
 * Caso de uso (puerto de entrada) que devuelve el número de fichajes
 * registrados hoy en la empresa del usuario autenticado.
 * <p>
 * Alimenta los indicadores del panel de administración. La consulta se filtra
 * siempre por la compañía del usuario autenticado para garantizar el
 * aislamiento multi-tenant.
 */
public interface GetTimeEntryCountTodayUseCase {

    /**
     * Cuenta los fichajes registrados hoy en la empresa autenticada.
     *
     * @return El número total de fichajes del día de hoy.
     */
    Long execute();
}
