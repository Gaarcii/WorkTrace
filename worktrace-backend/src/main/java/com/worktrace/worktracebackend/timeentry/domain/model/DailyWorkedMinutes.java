package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalDate;

/**
 * Minutos trabajados por un empleado en un día concreto.
 * <p>
 * Resultado de la agregación por día que devuelve la consulta de minutos
 * trabajados en un rango; se usa para construir el desglose de estadísticas sin
 * recorrer fichaje a fichaje.
 *
 * @param date    Día al que corresponden los minutos.
 * @param minutes Minutos trabajados ese día.
 */
public record DailyWorkedMinutes(
        LocalDate date,
        long minutes
) {
}
