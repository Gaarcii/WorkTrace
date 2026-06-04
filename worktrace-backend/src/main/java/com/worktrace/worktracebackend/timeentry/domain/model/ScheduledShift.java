package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Turno previsto de un trabajador para un día de la semana.
 * <p>
 * Modelo de dominio que aporta la hora de entrada planificada según el horario
 * del trabajador. Se usa como referencia para calcular la puntualidad de un
 * fichaje activo.
 *
 * @param employeeId Identificador del empleado.
 * @param startTime  Hora de entrada prevista en su horario.
 */
public record ScheduledShift(
        UUID employeeId,
        LocalTime startTime
) {
}
