package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resultado de registrar un evento de fichaje.
 * <p>
 * Modelo de dominio que refleja el estado del fichaje afectado tras procesar el
 * evento: el que se acaba de abrir (entrada) o el que se acaba de cerrar
 * (salida). La capa web lo traduce a su DTO de respuesta.
 *
 * @param id      Identificador del fichaje afectado.
 * @param startAt Marca temporal de la hora de entrada.
 * @param endAt   Marca temporal de la hora de salida, o {@code null} si la
 *                jornada sigue abierta.
 * @param status  Estado del fichaje ({@code OPEN} o {@code CLOSED}).
 */
public record ClockEventResult(
        UUID id,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String status
) {
}
