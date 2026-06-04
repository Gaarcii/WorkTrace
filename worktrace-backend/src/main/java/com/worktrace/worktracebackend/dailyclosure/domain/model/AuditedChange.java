package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.util.UUID;

/**
 * Cambio auditado sobre un fichaje, usado en la verificación de integridad.
 * <p>
 * Representa una modificación registrada en el log de auditoría tras el cierre,
 * incluyendo el estado anterior del fichaje ({@code oldSnapshot}) que permite
 * reconstruir cómo estaba en el momento del cierre y comprobar si la
 * discrepancia del hash queda explicada.
 *
 * @param timeEntryId Identificador del fichaje modificado.
 * @param action      Tipo de acción auditada (p. ej. {@code ADMIN_ADJUST},
 *                    {@code SOFT_DELETE}, {@code DB_DIRECT_MODIFY}).
 * @param oldSnapshot Estado previo del fichaje antes del cambio.
 */
public record AuditedChange(
        UUID timeEntryId,
        String action,
        TimeEntrySnapshot oldSnapshot
) {}
