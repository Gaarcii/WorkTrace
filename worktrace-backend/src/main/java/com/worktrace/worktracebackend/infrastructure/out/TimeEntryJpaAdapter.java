package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.timeentry.domain.model.ActiveTimeEntry;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyEntryCount;
import com.worktrace.worktracebackend.timeentry.domain.model.LastTimeEntries;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Adaptador de salida JPA para las consultas de fichajes.
 * <p>
 * Implementa los dos puertos de consulta de fichajes del proyecto: el del
 * dominio de cierre diario ({@link TimeEntryQueryPort}) y el del dominio de
 * fichajes ({@code timeentry.domain.port.out.TimeEntryQueryPort}). Traduce las
 * entidades JPA a los modelos de dominio (p. ej. {@link TimeEntrySnapshot}) y
 * mantiene todas las consultas como solo lectura.
 */
@Component
@Transactional(readOnly = true)
public class TimeEntryJpaAdapter implements TimeEntryQueryPort, com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryJpaAdapter(TimeEntryRepository timeEntryRepository) {
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

    /**
     * {@inheritDoc}
     * <p>
     * Cuenta los fichajes de una empresa en una fecha concreta.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha sobre la que se realiza el recuento.
     * @return El número de fichajes de la empresa en esa fecha.
     */
    @Override
    public Long countByCompanyAndDate(UUID companyId, LocalDate date) {
        return timeEntryRepository.countByCompany_IdAndWorkDate(companyId, date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Suma los minutos trabajados por una empresa en una fecha concreta.
     *
     * @param companyId Identificador de la empresa.
     * @param date      Fecha sobre la que se calculan los minutos trabajados.
     * @return El total de minutos trabajados por la empresa en esa fecha.
     */
    @Override
    public Long getWorkedMinutesByCompanyAndDate(UUID companyId, LocalDate date) {
        return timeEntryRepository.getWorkedMinutesByCompanyAndDate(companyId, date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtiene la fecha del primer fichaje registrado por un empleado.
     *
     * @param userId Identificador del usuario/empleado.
     * @return La fecha del primer fichaje, o {@code null} si no tiene ninguno.
     */
    @Override
    public LocalDate findFirstWorkDateByEmployee(UUID userId) {
        return timeEntryRepository.findFirstWorkDateByEmployee(userId);
    }

    @Override
    public List<DailyEntryCount> countByCompanyAndDateRange(UUID companyId, LocalDate start, LocalDate end) {
        return timeEntryRepository.getTimeEntryCountByCompanyAndDateRange(companyId, start, end)
                .stream()
                .map(p -> new DailyEntryCount(p.getFecha(), p.getNumFichajes()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera los fichajes de la empresa sin hora de salida (jornadas abiertas)
     * y los traduce a {@link ActiveTimeEntry}, resolviendo el puesto del empleado
     * o {@code null} si no tiene asignado.
     */
    @Override
    public List<ActiveTimeEntry> findActiveByCompany(UUID companyId) {
        return timeEntryRepository.getAllByCompany_IdAndEndAtIsNull(companyId)
                .stream()
                .map(p -> new ActiveTimeEntry(p.getEmployee().getUserId(), p.getEmployee().getFullName(),
                        p.getEmployee().getPosition() != null ? p.getEmployee().getPosition().getTitle() : null,
                        p.getEmployee().getAvatarUrl(), p.getStartAt()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recupera los 5 fichajes más recientes del empleado y los descompone en
     * eventos individuales: una "Entrada" por cada fichaje y una "Salida" si ya
     * tiene hora de fin. El resultado se limita a 5 eventos.
     */
    @Override
    public List<LastTimeEntries> findTop5ByEmployee(UUID userId) {
        return timeEntryRepository.findTop5ByEmployee_UserIdOrderByStartAtDesc(userId)
                .stream()
                .flatMap(te -> {
                    List<LastTimeEntries> events = new ArrayList<>();
                    events.add(new LastTimeEntries(te.getId(), "Entrada", te.getStartAt()));
                    if (te.getEndAt() != null) {
                        events.add(new LastTimeEntries(te.getId(), "Salida", te.getEndAt()));
                    }
                    return events.stream();
                }).limit(5)
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Suma, a nivel de persistencia, los minutos trabajados por el empleado en la
     * fecha dada.
     */
    @Override
    public long getWorkedMinutesByEmployeeAndDate(UUID userId, LocalDate date) {
        return timeEntryRepository.getWorkedMinutesByEmployeeAndDate(userId, date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca el fichaje abierto ({@code OPEN}, sin hora de salida) del empleado y
     * lo traduce a {@link ActiveTimeEntry}, resolviendo el puesto o {@code null}
     * si no tiene asignado.
     */
    @Override
    public Optional<ActiveTimeEntry> findOpenByEmployee(UUID userId) {
        return timeEntryRepository.findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(userId, TimeEntryStatus.OPEN)
                .map(te -> new ActiveTimeEntry(
                        te.getEmployee().getUserId(),
                        te.getEmployee().getFullName(),
                        te.getEmployee().getPosition() != null ? te.getEmployee().getPosition().getTitle() : null,
                        te.getEmployee().getAvatarUrl(),
                        te.getStartAt()
                ));
    }

}
