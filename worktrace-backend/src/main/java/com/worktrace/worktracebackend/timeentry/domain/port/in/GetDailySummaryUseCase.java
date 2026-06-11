package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.DailySummary;

/**
 * Caso de uso (puerto de entrada) que obtiene el resumen diario de fichajes del
 * trabajador autenticado.
 * <p>
 * Reúne el estado del día del propio trabajador: minutos acumulados, objetivo de
 * jornada según su horario, hora de entrada si tiene una jornada en curso y sus
 * últimos eventos de fichaje. Alimenta la vista personal del trabajador.
 */
public interface GetDailySummaryUseCase {

    /**
     * Calcula el resumen diario del trabajador autenticado.
     *
     * @return El {@link DailySummary} con el estado del día del trabajador.
     */
    DailySummary execute();
}
