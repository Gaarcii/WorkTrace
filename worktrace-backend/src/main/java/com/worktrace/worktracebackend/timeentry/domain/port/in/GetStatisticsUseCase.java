package com.worktrace.worktracebackend.timeentry.domain.port.in;

import com.worktrace.worktracebackend.timeentry.domain.model.WorkStatistics;

import java.time.LocalDate;

/**
 * Caso de uso (puerto de entrada) que calcula las estadísticas de trabajo del
 * trabajador autenticado en un rango de fechas.
 * <p>
 * Produce el desglose día a día (trabajado frente a planificado) y los totales
 * del periodo: minutos trabajados, balance respecto al objetivo y número de
 * jornadas incompletas. Alimenta la vista de estadísticas del trabajador.
 */
public interface GetStatisticsUseCase {

    /**
     * Calcula las estadísticas de trabajo del trabajador autenticado en el rango
     * indicado (ambos extremos incluidos).
     *
     * @param startDate Fecha de inicio del rango.
     * @param endDate   Fecha de fin del rango.
     * @return El {@link WorkStatistics} con el desglose diario y los totales.
     */
    WorkStatistics execute(LocalDate startDate, LocalDate endDate);
}
