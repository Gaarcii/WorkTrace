package com.worktrace.worktracebackend.timeentry.domain.port.in;

/**
 * Caso de uso (puerto de entrada) que calcula el total de minutos trabajados
 * hoy por todos los empleados de la empresa del usuario autenticado.
 * <p>
 * Alimenta el indicador de horas trabajadas del panel de administración. La
 * consulta se filtra siempre por la compañía del usuario autenticado para
 * garantizar el aislamiento multi-tenant.
 */
public interface GetTotalHoursTodayUseCase {

    /**
     * Suma los minutos trabajados hoy en la empresa autenticada.
     *
     * @return El total de minutos trabajados en el día de hoy.
     */
    Long execute();
}
