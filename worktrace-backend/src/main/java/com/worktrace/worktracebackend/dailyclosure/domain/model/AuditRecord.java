package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro plano del log de auditoría usado en la comprobación de integridad.
 * <p>
 * Versión ligera de un cambio auditado (sin el snapshot completo) que aporta
 * únicamente qué fichaje cambió, con qué acción y cuándo, suficiente para
 * detectar la presencia de modificaciones directas en base de datos.
 *
 * @param timeEntryId Identificador del fichaje afectado.
 * @param action      Tipo de acción auditada (p. ej. {@code ADMIN_ADJUST},
 *                    {@code DB_DIRECT_MODIFY}).
 * @param createdAt   Instante en que se registró el cambio.
 */
public record AuditRecord(
        UUID timeEntryId,
        String action,
        OffsetDateTime createdAt
) {}
