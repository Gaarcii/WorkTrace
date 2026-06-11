package com.worktrace.worktracebackend.timeentry.domain.model;

import java.util.List;

/**
 * Historial de fichajes de un trabajador para un día concreto.
 * <p>
 * Modelo de dominio que agrega, para la vista de historial, el progreso del día
 * y de la semana (trabajado frente a objetivo) junto con los eventos de fichaje
 * del día.
 *
 * @param dailyWorkedMinutes  Minutos trabajados por el trabajador ese día.
 * @param dailyTargetMinutes  Minutos objetivo de la jornada ese día según su
 *                            horario; 0 si no tiene horario definido.
 * @param weeklyWorkedMinutes Minutos trabajados en la semana de la fecha (de
 *                            lunes a domingo).
 * @param weeklyTargetMinutes Minutos objetivo semanales según las horas
 *                            contratadas del trabajador; 0 si no constan.
 * @param dailyRecords        Eventos de fichaje del día (entradas y salidas).
 */
public record DailyHistory(
        long dailyWorkedMinutes,
        long dailyTargetMinutes,
        long weeklyWorkedMinutes,
        long weeklyTargetMinutes,
        List<LastTimeEntries> dailyRecords
) {
}
