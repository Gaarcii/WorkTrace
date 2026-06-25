package com.worktrace.worktracebackend.dto.timeEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Projection cerrada para la tabla paginada de fichajes de un empleado.
 * <p>
 * Selecciona únicamente las columnas que necesita la vista, evitando materializar
 * la entidad {@code TimeEntry} completa (geoip, user-agent, ips, etc.) en
 * consultas que pueden devolver muchas páginas.
 */
public interface TimeEntryRowProjection {
    UUID getId();
    LocalDate getWorkDate();
    OffsetDateTime getStartAt();
    OffsetDateTime getEndAt();
    BigDecimal getStartLat();
    BigDecimal getStartLng();
    BigDecimal getEndLat();
    BigDecimal getEndLng();
}
