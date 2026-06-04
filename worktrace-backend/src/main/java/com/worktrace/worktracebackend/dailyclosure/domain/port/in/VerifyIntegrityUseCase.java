package com.worktrace.worktracebackend.dailyclosure.domain.port.in;

import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Caso de uso (puerto de entrada) que verifica la integridad de un cierre diario
 * de una empresa para una fecha concreta.
 * <p>
 * Permite a administradores e inspectores comprobar si los fichajes de un día
 * cerrado se han mantenido íntegros o han sufrido modificaciones.
 */
public interface VerifyIntegrityUseCase {

    /**
     * Verifica la integridad del cierre de una empresa en una fecha.
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @param date      Fecha del cierre a verificar.
     * @return El {@link IntegrityResult} con el estado de integridad calculado.
     */
    IntegrityResult execute(UUID companyId, LocalDate date);
}
