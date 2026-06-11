package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalTime;

/**
 * Turno previsto de un trabajador para un día de la semana.
 * <p>
 * Modelo de dominio que aporta las horas de entrada y salida planificadas según
 * el horario del trabajador. Sirve de referencia para calcular la puntualidad de
 * un fichaje activo (hora de entrada) y el objetivo de jornada del resumen diario
 * (duración entre entrada y salida).
 *
 * @param startTime Hora de entrada prevista en su horario.
 * @param endTime   Hora de salida prevista en su horario.
 */
public record ScheduledShift(
        LocalTime startTime,
        LocalTime endTime
) {
}
