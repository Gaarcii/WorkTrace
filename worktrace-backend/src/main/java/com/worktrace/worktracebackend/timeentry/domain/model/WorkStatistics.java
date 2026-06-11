package com.worktrace.worktracebackend.timeentry.domain.model;

import java.util.List;

/**
 * Estadísticas de trabajo de un trabajador para un periodo.
 * <p>
 * Modelo de dominio que agrega los totales del rango y el desglose día a día,
 * para la vista de estadísticas del trabajador.
 *
 * @param totalWorkedMinutes Total de minutos trabajados en todo el periodo.
 * @param minutesBalance     Balance del periodo: minutos trabajados menos
 *                           planificados (negativo si se trabajó de menos,
 *                           positivo si de más).
 * @param incompleteWorkdays Número de jornadas con turno previsto en las que se
 *                           trabajó menos de lo planificado.
 * @param dailySummary       Desglose diario del periodo.
 */
public record WorkStatistics(
        long totalWorkedMinutes,
        long minutesBalance,
        int incompleteWorkdays,
        List<DailyStatistic> dailySummary
) {
}
