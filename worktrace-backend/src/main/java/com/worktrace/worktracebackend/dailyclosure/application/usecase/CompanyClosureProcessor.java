package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureAlreadyExistsException;
import com.worktrace.worktracebackend.dailyclosure.domain.exception.OpenShiftsExistException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.HashResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Servicio de aplicación que ejecuta el cierre diario de <strong>una única
 * compañía</strong> para una fecha concreta.
 * <p>
 * Es la unidad de trabajo transaccional del proceso de cierre: consolida los
 * fichajes del día, valida las precondiciones de negocio y produce un registro
 * de cierre ({@link DailyClosureRecord}) sellado con un hash criptográfico
 * encadenado al cierre del día anterior. Ese encadenamiento de hashes es la base
 * de la protección anti-fraude, ya que cualquier modificación retroactiva de un
 * fichaje rompe la cadena y se vuelve detectable.
 *
 * <p><strong>Reglas de negocio aplicadas (en orden):</strong>
 * <ol>
 *   <li>No puede existir ya un cierre para la compañía y fecha indicadas
 *       (idempotencia: un día se cierra una sola vez).</li>
 *   <li>No pueden quedar turnos abiertos: todas las jornadas deben estar
 *       finalizadas antes de sellar el día.</li>
 * </ol>
 *
 * <p><strong>Frontera transaccional:</strong> {@code process} se ejecuta con
 * {@link Propagation#REQUIRES_NEW}, de modo que el cierre de cada compañía vive
 * en su propia transacción. Así, un fallo (o rollback) en una compañía no
 * arrastra ni contamina el cierre de las demás cuando este procesador se invoca
 * en paralelo desde {@link RunDailyClosureUseCaseImpl}.
 *
 * @see RunDailyClosureUseCaseImpl
 * @see DailyHashChain
 */
@Service
public class CompanyClosureProcessor {

    /**
     * Hash semilla usado como eslabón inicial de la cadena cuando la compañía
     * aún no tiene ningún cierre anterior (primer día que se cierra).
     */
    private static final String GENESIS_HASH = "GENESIS_HASH_0000000000000000000000000000";

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;

    /**
     * @param timeEntryQueryPort Puerto de consulta de fichajes (turnos abiertos
     *                           y snapshots ordenados para el cierre).
     * @param dailyClosurePort   Puerto de persistencia/consulta de cierres
     *                           diarios (existencia, hash previo y guardado).
     */
    public CompanyClosureProcessor(
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain();
    }


    /**
     * Ejecuta el cierre diario de una compañía para la fecha indicada.
     * <p>
     * Flujo:
     * <ol>
     *   <li>Verifica que no exista ya un cierre para esa fecha; si existe, aborta.</li>
     *   <li>Verifica que no queden turnos abiertos; si los hay, aborta.</li>
     *   <li>Recupera el hash del cierre anterior, o usa {@link #GENESIS_HASH} si
     *       es el primer cierre de la compañía.</li>
     *   <li>Recorre los fichajes del día en orden determinista (vía un
     *       {@link Stream} que se cierra con try-with-resources) y calcula el
     *       hash encadenado con {@link DailyHashChain}.</li>
     *   <li>Construye y persiste el {@link DailyClosureRecord} resultante,
     *       sellando la fecha y el instante de cómputo.</li>
     * </ol>
     * Se ejecuta en una transacción nueva e independiente
     * ({@link Propagation#REQUIRES_NEW}).
     *
     * @param companyId  Identificador de la compañía cuyo cierre se procesa
     *                   (aislamiento multi-tenant).
     * @param targetDate Fecha laboral sobre la que se consolida el cierre.
     * @throws DailyClosureAlreadyExistsException si ya existe un cierre para esa
     *                                            compañía y fecha.
     * @throws OpenShiftsExistException           si quedan turnos abiertos en la
     *                                            fecha objetivo.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID companyId, LocalDate targetDate) {
        if (dailyClosurePort.existsForDate(companyId, targetDate)) {
            throw new DailyClosureAlreadyExistsException(targetDate);
        }

        long openShifts = timeEntryQueryPort.countOpenShifts(companyId, targetDate);
        if (openShifts > 0) {
            throw new OpenShiftsExistException(openShifts);
        }

        String prevHash = dailyClosurePort.findPreviousHash(companyId, targetDate)
                .orElse(GENESIS_HASH);

        HashResult result;
        try (Stream<TimeEntrySnapshot> stream = timeEntryQueryPort.findOrderedForClosure(companyId, targetDate)) {
            result = dailyHashChain.compute(stream, prevHash);
        }

        DailyClosureRecord record = new DailyClosureRecord(
                companyId,
                targetDate,
                result.hash(),
                prevHash,
                result.recordCount(),
                OffsetDateTime.now()
        );

        dailyClosurePort.save(record);
    }
}
