package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.*;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Implementación del caso de uso {@link VerifyIntegrityUseCase}, que verifica la
 * integridad criptográfica de un cierre diario ya calculado.
 * <p>
 * Recalcula el hash encadenado de los fichajes actuales (vía
 * {@link DailyHashChain}) y lo compara con el hash almacenado en el cierre. Si
 * difieren, cruza el resultado con el log de auditoría para distinguir entre
 * modificaciones legítimas (registradas por la aplicación) y manipulaciones no
 * trazadas, devolviendo un {@link IntegrityResult}.
 * <p>
 * Toda la operación se ejecuta como transacción de solo lectura y filtrada por
 * compañía, garantizando el aislamiento multi-tenant.
 */
@Service
public class VerifyIntegrityUseCaseImpl implements VerifyIntegrityUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;
    private final AuditQueryPort auditQueryPort;

    public VerifyIntegrityUseCaseImpl(
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort,
            AuditQueryPort auditQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain();
        this.auditQueryPort = auditQueryPort;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Aplica la siguiente lógica de decisión sobre el cierre de la compañía y
     * fecha indicadas:
     * <ol>
     *   <li>Si no existe cierre para la fecha, lanza
     *       {@link DailyClosureNotFoundException}.</li>
     *   <li>Recalcula el hash de los fichajes actuales. Si coincide con el hash
     *       almacenado, devuelve {@link IntegrityResult#VALID}.</li>
     *   <li>Si difiere y no hay ningún cambio en auditoría posterior al cierre,
     *       devuelve {@link IntegrityResult#CORRUPTED}.</li>
     *   <li>Si entre los cambios auditados hay alguno marcado como
     *       {@code DB_DIRECT_MODIFY} (modificación directa en BD), devuelve
     *       {@link IntegrityResult#CORRUPTED}.</li>
     *   <li>En caso contrario, los cambios se consideran modificaciones
     *       legítimas registradas por la aplicación y devuelve
     *       {@link IntegrityResult#MODIFIED}.</li>
     * </ol>
     *
     * @param companyId Identificador de la empresa (aislamiento multi-tenant).
     * @param date      Fecha del cierre a verificar.
     * @return El estado de integridad calculado para el cierre.
     * @throws DailyClosureNotFoundException si no existe cierre para esa fecha.
     */
    @Override
    @Transactional(readOnly = true)
    public IntegrityResult execute(UUID companyId, LocalDate date) {
        DailyClosureRecord record = dailyClosurePort.findByDate(companyId, date)
                .orElseThrow(() -> new DailyClosureNotFoundException(date));

        List<TimeEntrySnapshot> currentSnapshots;
        try (Stream<TimeEntrySnapshot> stream = timeEntryQueryPort.findOrderedForClosure(companyId, date)) {
            currentSnapshots = stream.toList();
        }
        HashResult result = dailyHashChain.compute(currentSnapshots.stream(), record.prevDayHash());

        if (result.hash().equals(record.dayHash())) {
            return IntegrityResult.VALID;
        }

        List<AuditRecord> auditRecords = auditQueryPort.findAllChangesForIntegrityCheck(companyId, date, record.computedAt());
        if (auditRecords.isEmpty()) {
            return IntegrityResult.CORRUPTED;
        }

        boolean hasDirectModify = auditRecords.stream()
                .anyMatch(audit -> "DB_DIRECT_MODIFY".equals(audit.action()));

        if (hasDirectModify) {
            return IntegrityResult.CORRUPTED;
        }

        return IntegrityResult.MODIFIED;
    }
}
