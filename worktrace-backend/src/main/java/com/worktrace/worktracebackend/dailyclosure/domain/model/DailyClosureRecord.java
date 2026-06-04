package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de dominio que representa el cierre diario de una compañía.
 * <p>
 * Es el resultado inmutable del proceso de cierre: contiene el hash del día
 * encadenado con el del día anterior ({@code prevDayHash}), formando la cadena
 * de integridad que permite detectar manipulaciones retroactivas.
 *
 * @param companyId    Identificador de la empresa (obligatorio).
 * @param workDate     Fecha laboral a la que corresponde el cierre (obligatorio).
 * @param dayHash      Hash de integridad calculado para el día (obligatorio).
 * @param prevDayHash  Hash del cierre del día anterior con el que se encadena.
 * @param recordsCount Número de fichajes incluidos en el cierre.
 * @param computedAt   Instante en que se calculó el cierre (obligatorio).
 */
public record DailyClosureRecord(
        UUID companyId,
        LocalDate workDate,
        String dayHash,
        String prevDayHash,
        int recordsCount,
        OffsetDateTime computedAt
) {
    /**
     * Valida que los campos obligatorios no sean nulos.
     *
     * @throws NullPointerException si {@code companyId}, {@code workDate},
     *                              {@code dayHash} o {@code computedAt} son nulos.
     */
    public DailyClosureRecord {
        Objects.requireNonNull(companyId, "El companyId no puede ser nulo");
        Objects.requireNonNull(workDate, "La fecha de trabajo es obligatoria");
        Objects.requireNonNull(dayHash, "El hash del día no puede estar vacío");
        Objects.requireNonNull(computedAt, "La fecha de computado es obligatoria");
    }


}
