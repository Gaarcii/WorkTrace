package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Adaptador de salida JPA para las consultas de fichajes del dominio de cierre
 * diario.
 * <p>
 * Implementa el {@link TimeEntryQueryPort} del cierre, traduciendo las entidades
 * JPA a {@link TimeEntrySnapshot} y manteniendo todas las consultas como solo
 * lectura. Está separado del adaptador de consultas de fichajes del panel para
 * que cada adaptador sirva a un único bounded context.
 */
@Component
@Transactional(readOnly = true)
public class TimeEntryClosureJpaAdapter implements TimeEntryQueryPort {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryClosureJpaAdapter(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cuenta los fichajes aún abiertos (sin salida) de una empresa en una fecha,
     * usado para validar que un día puede cerrarse.
     *
     * @param companyId  Identificador de la empresa.
     * @param targetDate Fecha sobre la que se cuentan los turnos abiertos.
     * @return El número de fichajes en estado {@code OPEN}.
     */
    @Override
    public long countOpenShifts(UUID companyId, LocalDate targetDate) {
        return timeEntryRepository.countByCompanyIdAndWorkDateAndTimeEntryStatus(
                companyId, targetDate, TimeEntryStatus.OPEN);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Devuelve los fichajes de una empresa y fecha en el orden determinista
     * requerido para el cálculo del hash del cierre, como flujo de
     * {@link TimeEntrySnapshot}.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha del cierre.
     * @return Un {@link Stream} de snapshots ordenados para el cierre.
     */
    @Override
    public Stream<TimeEntrySnapshot> findOrderedForClosure(UUID companyId, LocalDate date) {
        return timeEntryRepository
                .streamForClosure(companyId, date)
                .map(this::toSnapshot);
    }

    /**
     * Convierte una entidad {@link TimeEntry} en un {@link TimeEntrySnapshot}
     * inmutable, capturando el estado completo del fichaje para el cierre y la
     * auditoría.
     *
     * @param entity Entidad de fichaje persistida.
     * @return El snapshot de dominio equivalente.
     */
    private TimeEntrySnapshot toSnapshot(TimeEntry entity) {
        return new TimeEntrySnapshot(
                entity.getId(),
                entity.getEmployee().getUserId(),
                entity.getWorkDate(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getStartLat(),
                entity.getStartLng(),
                entity.getEndLat(),
                entity.getEndLng(),
                entity.getStartAccuracyM(),
                entity.getEndAccuracyM(),
                entity.getStartIp(),
                entity.getEndIp(),
                entity.getStartUserAgent(),
                entity.getEndUserAgent(),
                entity.getStartGeoip(),
                entity.getEndGeoip(),
                entity.getFlags(),
                entity.getTimeEntryStatus(),
                entity.getDeletedAt(),
                entity.getDeletedBy() != null ? entity.getDeletedBy().getId() : null,
                entity.getDeleteReason(),
                entity.getCreatedAt(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.getUpdatedAt(),
                entity.getModificationReason(),
                entity.getCompany().getId()
        );
    }
}
