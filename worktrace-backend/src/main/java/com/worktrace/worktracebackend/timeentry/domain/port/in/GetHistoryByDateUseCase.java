package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.DailyHistory;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que obtiene el historial de fichajes del
 * trabajador autenticado para un día concreto.
 * <p>
 * Además de los eventos del día, agrega los indicadores de progreso diario y
 * semanal (minutos trabajados frente a objetivo), tomando como semana la que
 * contiene la fecha consultada (de lunes a domingo). Alimenta la vista de
 * historial del trabajador.
 */
public interface GetHistoryByDateUseCase {

    /**
     * Calcula el historial y los indicadores del día indicado para el trabajador
     * autenticado.
     *
     * @param date Fecha cuyo historial se consulta.
     * @return El {@link DailyHistory} con los eventos del día y los totales
     * diario y semanal.
     */
    DailyHistory execute(LocalDate date);
}
