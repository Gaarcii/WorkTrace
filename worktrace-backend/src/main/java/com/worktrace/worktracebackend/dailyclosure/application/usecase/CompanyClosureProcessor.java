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
 * Servicio de aplicación encargado de ejecutar el cierre diario completo de una compañía.
 * Este componente orquesta la regla de negocio que consolida las fichas del día,
 * verifica que no existan jornadas abiertas, calcula la cadena de hash de integridad
 * y persiste el registro final del cierre para el dominio de {@code DailyClosure}.
 * La operación se ejecuta en una transacción independiente para aislar el cierre
 * de cada compañía y garantizar que un fallo no afecte al resto de ejecuciones.
 */
@Service
public class CompanyClosureProcessor {

    private static final String GENESIS_HASH = "GENESIS_HASH_0000000000000000000000000000";

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;

    public CompanyClosureProcessor(
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain();
    }

    /**
     * Ejecuta el cierre diario de una compañía para una fecha concreta, aplicando las reglas
     * de integridad y consistencia antes de registrar el resultado final.
     * La operación valida primero que no exista ya un cierre para la fecha indicada y que no
     * permanezcan jornadas abiertas. Si ambas comprobaciones son correctas, obtiene el hash
     * previo, calcula la nueva cadena de integridad a partir de las fichas ordenadas y guarda
     * el resumen del cierre diario.
     *
     * @param companyId  identificador de la compañía cuyo cierre diario se va a procesar.
     * @param targetDate fecha objetivo sobre la que se debe consolidar el cierre diario.
     * @throws DailyClosureAlreadyExistsException si ya existe un cierre diario registrado para
     *                                            la compañía en la fecha indicada.
     * @throws OpenShiftsExistException           si todavía existen jornadas abiertas para la compañía
     *                                            en la fecha objetivo, impidiendo el cierre.
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
