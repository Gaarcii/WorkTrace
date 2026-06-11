package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.Duration;
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

    /**
     * Calcula la duración prevista del turno en minutos.
     * <p>
     * Si el turno cruza la medianoche (la hora de fin es anterior a la de
     * inicio), se le suma un día para obtener una duración positiva.
     *
     * @return Los minutos de jornada previstos entre la entrada y la salida.
     */
    public long durationMinutes() {
        Duration d = Duration.between(startTime, endTime);
        if (d.isNegative()) d = d.plusDays(1);
        return d.toMinutes();
    }
}
