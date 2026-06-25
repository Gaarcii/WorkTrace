package com.worktrace.worktracebackend.timeentry.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Fila de la tabla paginada de fichajes de un empleado.
 * <p>
 * Modelo de dominio ligero con solo los datos que necesita la vista de tabla
 * (sin geoip, user-agent ni ips), para no materializar la entidad completa en
 * consultas que pueden devolver muchas páginas.
 *
 * @param id         Identificador del fichaje.
 * @param date       Fecha laboral del fichaje.
 * @param startAt    Marca temporal de entrada.
 * @param endAt      Marca temporal de salida (nula si el fichaje sigue abierto).
 * @param latStartAt Latitud capturada en la entrada.
 * @param lngStartAt Longitud capturada en la entrada.
 * @param latEndAt   Latitud capturada en la salida.
 * @param lngEndAt   Longitud capturada en la salida.
 */
public record TimeEntryRow(
        UUID id,
        LocalDate date,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BigDecimal latStartAt,
        BigDecimal lngStartAt,
        BigDecimal latEndAt,
        BigDecimal lngEndAt
) {

    /**
     * Calcula los minutos trabajados del fichaje.
     *
     * @return Los minutos entre la entrada y la salida, o {@code null} si el
     * fichaje aún no tiene entrada o salida.
     */
    public Long workedMinutes() {
        return WorkedMinutesHelper.getWorkedMinutes(startAt, endAt);
    }
}