package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.ActiveWorker;

import java.util.List;

/**
 * Caso de uso (puerto de entrada) que obtiene los trabajadores que tienen
 * actualmente una jornada de trabajo abierta en la empresa del usuario
 * autenticado.
 * <p>
 * Para cada trabajador activo, además de sus datos, calcula su puntualidad
 * respecto al horario previsto del día. Se usa en el panel de administración
 * para la vista en tiempo real de quién está fichado. La consulta se filtra
 * siempre por la compañía del usuario autenticado (aislamiento multi-tenant).
 */
public interface GetActiveWorkersUseCase {

    /**
     * Obtiene los trabajadores con jornada abierta en la empresa autenticada.
     *
     * @return La lista de {@link ActiveWorker} activos, con su puntualidad
     * calculada; lista vacía si no hay ninguno.
     */
    List<ActiveWorker> execute();
}
