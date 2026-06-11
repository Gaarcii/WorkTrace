package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalDate;

/**
 * Estadística de un día concreto dentro de un periodo de estadísticas de
 * trabajo.
 * <p>
 * Compara lo trabajado con lo planificado para ese día, base del desglose diario
 * que se muestra al trabajador.
 *
 * @param date           Día al que corresponde la estadística.
 * @param workedMinutes  Minutos efectivamente trabajados ese día.
 * @param plannedMinutes Minutos planificados según el horario ese día; 0 si no
 *                       hay turno previsto.
 */
public record DailyStatistic(
        LocalDate date,
        long workedMinutes,
        long plannedMinutes

) {
}
