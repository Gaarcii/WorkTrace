package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditRecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida para consultar el log de auditoría de fichajes.
 * <p>
 * Da soporte a la verificación de integridad permitiendo recuperar los cambios
 * registrados sobre los fichajes después de que un cierre haya sido calculado.
 */
public interface AuditQueryPort {

    /**
     * Recupera, por fichaje, el primer cambio auditado posterior al cierre,
     * incluyendo el estado anterior necesario para reconstruir la situación en
     * el momento del cierre.
     *
     * @param companyId         Identificador de la empresa.
     * @param date              Fecha laboral de los fichajes.
     * @param closureComputedAt Instante de cálculo del cierre; solo se consideran
     *                          cambios posteriores.
     * @return La lista de cambios auditados (uno por fichaje).
     */
    List<AuditedChange> getChangesAfterClosure(UUID companyId, LocalDate date, OffsetDateTime closureComputedAt);

    /**
     * Recupera, de forma plana, todos los cambios auditados de los fichajes
     * posteriores al cierre, para detectar la presencia de modificaciones
     * directas en base de datos.
     *
     * @param companyId         Identificador de la empresa.
     * @param workDate          Fecha laboral de los fichajes.
     * @param closureComputedAt Instante de cálculo del cierre; solo se consideran
     *                          cambios posteriores.
     * @return La lista de registros de auditoría relevantes para la integridad.
     */
    List<AuditRecord> findAllChangesForIntegrityCheck(UUID companyId, LocalDate workDate, OffsetDateTime closureComputedAt);
}
