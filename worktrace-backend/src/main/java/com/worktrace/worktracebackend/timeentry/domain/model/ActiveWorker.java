package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Trabajador actualmente activo, resultado de la consulta de fichados en curso.
 * <p>
 * Enriquece un {@link ActiveTimeEntry} con la puntualidad del trabajador
 * respecto a su horario previsto. Es el modelo que consume el panel de
 * administración.
 *
 * @param employeeId         Identificador del empleado.
 * @param fullName           Nombre completo del trabajador.
 * @param jobPosition        Puesto de trabajo (puede ser {@code null}).
 * @param avatarUrl          URL del avatar del trabajador.
 * @param entryTime          Marca temporal de entrada de la jornada en curso.
 * @param punctualityMinutes Diferencia en minutos entre la hora de entrada real
 *                           y la prevista en su horario: negativo si se adelantó,
 *                           positivo si llegó tarde. Es {@code null} cuando el
 *                           trabajador no tiene horario definido para ese día.
 */
public record ActiveWorker(
        UUID employeeId,
        String fullName,
        String jobPosition,
        String avatarUrl,
        OffsetDateTime entryTime,
        Long punctualityMinutes
) {
}
