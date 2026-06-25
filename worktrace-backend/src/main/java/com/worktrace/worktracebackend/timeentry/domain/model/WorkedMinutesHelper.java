package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Utilidad de dominio para calcular los minutos trabajados de un fichaje.
 * <p>
 * Centraliza la lógica de duración entre entrada y salida que comparten varios
 * modelos de fichajes (p. ej. {@link TimeEntryRow} y {@link AdminByDate}),
 * evitando duplicarla. Es una clase de utilidad no instanciable.
 */
public class WorkedMinutesHelper {

    private WorkedMinutesHelper() {
        // Utility class
    }

    /**
     * Calcula los minutos transcurridos entre la entrada y la salida.
     *
     * @param startAt Marca temporal de entrada.
     * @param endAt   Marca temporal de salida.
     * @return Los minutos trabajados, o {@code null} si falta la entrada o la
     * salida (fichaje incompleto).
     */
    public static Long getWorkedMinutes(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null) {
            return null;
        }
        return Duration.between(startAt, endAt).toMinutes();
    }
}