package com.worktrace.worktracebackend.timeentry.domain.model;

import java.time.LocalDate;

/**
 * Rango de fechas resuelto para una consulta o informe.
 * <p>
 * Modelo de dominio que transporta un intervalo ya completo (sin nulos), con la
 * fecha de inicio y de fin efectivas tras aplicar los valores por defecto.
 *
 * @param start Fecha de inicio del rango (incluida).
 * @param end   Fecha de fin del rango (incluida).
 */
public record DateRange(
        LocalDate start,
        LocalDate end
) {
}
