package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Fila de la tabla de fichajes de la empresa por fecha (vista de administración).
 * <p>
 * Modelo de dominio que combina los datos del fichaje con los del perfil del
 * trabajador (nombre, puesto, avatar) que necesita la vista de administración,
 * sin materializar las entidades completas.
 *
 * @param id          Identificador del fichaje.
 * @param employeeId  Identificador del empleado.
 * @param workerName  Nombre del trabajador.
 * @param jobPosition Puesto de trabajo (puede ser {@code null}).
 * @param avatarUrl   URL del avatar del trabajador.
 * @param date        Fecha laboral del fichaje.
 * @param startAt     Marca temporal de entrada.
 * @param endAt       Marca temporal de salida (nula si el fichaje sigue abierto).
 */
public record AdminByDate(
        UUID id,
        UUID employeeId,
        String workerName,
        String jobPosition,
        String avatarUrl,
        LocalDate date,
        OffsetDateTime startAt,
        OffsetDateTime endAt
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
