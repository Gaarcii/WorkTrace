package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Fichaje actualmente abierto de un trabajador, con sus datos de perfil.
 * <p>
 * Modelo de dominio que representa una jornada en curso (sin hora de salida),
 * tal como la devuelve la consulta de fichajes activos. Es la entrada que el
 * caso de uso enriquece con la puntualidad para producir un {@link ActiveWorker}.
 *
 * @param employeeId  Identificador del empleado.
 * @param fullName    Nombre completo del trabajador.
 * @param jobPosition Puesto de trabajo (puede ser {@code null} si no tiene).
 * @param avatarUrl   URL del avatar del trabajador.
 * @param startAt     Marca temporal de entrada de la jornada abierta.
 */
public record ActiveTimeEntry(
        UUID employeeId,
        String fullName,
        String jobPosition,
        String avatarUrl,
        OffsetDateTime startAt
) {}
