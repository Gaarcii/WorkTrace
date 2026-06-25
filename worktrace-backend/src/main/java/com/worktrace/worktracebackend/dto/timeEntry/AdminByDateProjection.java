package com.worktrace.worktracebackend.dto.timeEntry;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Projection cerrada para la tabla de fichajes de la empresa por fecha.
 * <p>
 * Selecciona únicamente las columnas que necesita la vista de administración
 * (fichaje + datos básicos del trabajador), evitando materializar la entidad
 * {@code TimeEntry} completa (geoip, user-agent, ips, etc.) en consultas que
 * pueden devolver muchas páginas.
 */
public interface AdminByDateProjection {
    UUID getId();
    UUID getEmployeeId();
    String getWorkerName();
    String getJobPosition();
    String getAvatarUrl();
    LocalDate getDate();
    OffsetDateTime getStartAt();
    OffsetDateTime getEndAt();
}
