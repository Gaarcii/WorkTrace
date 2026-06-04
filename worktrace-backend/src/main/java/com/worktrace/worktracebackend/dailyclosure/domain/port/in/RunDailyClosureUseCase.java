package com.worktrace.worktracebackend.dailyclosure.domain.port.in;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que ejecuta el cierre diario de todas las
 * empresas para una fecha concreta.
 * <p>
 * Es el punto de entrada disparado por el planificador (o manualmente desde el
 * controlador). La implementación orquesta el cierre por empresa de forma
 * aislada y tolerante a fallos.
 */
public interface RunDailyClosureUseCase {

    /**
     * Ejecuta el cierre diario de todas las empresas para la fecha indicada.
     *
     * @param targetDate Fecha laboral cuyo cierre se desea procesar.
     */
    void execute(LocalDate targetDate);
}
