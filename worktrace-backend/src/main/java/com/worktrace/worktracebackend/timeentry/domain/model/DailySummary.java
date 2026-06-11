package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resumen diario de fichajes de un trabajador.
 * <p>
 * Modelo de dominio que agrega el estado del día del trabajador para su vista
 * personal: lo trabajado, el objetivo de jornada, si está actualmente fichado y
 * su historial reciente de eventos.
 *
 * @param accumulatedMinutes Minutos ya trabajados hoy por el trabajador.
 * @param targetMinutes      Minutos objetivo de la jornada según su horario; 0
 *                           si no tiene horario definido para hoy.
 * @param entryTime          Hora de entrada de la jornada en curso, o
 *                           {@code null} si no tiene ninguna jornada abierta.
 * @param lastTimeEntries    Últimos eventos de fichaje (entradas y salidas) del
 *                           trabajador, en orden cronológico inverso.
 */
public record DailySummary(
        long accumulatedMinutes,
        long targetMinutes,
        OffsetDateTime entryTime,
        List<LastTimeEntries> lastTimeEntries
) {
}
