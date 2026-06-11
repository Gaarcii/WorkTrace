package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.timeentry.domain.model.ActiveTimeEntry;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyEntryCount;
import com.worktrace.worktracebackend.timeentry.domain.model.DailyWorkedMinutes;
import com.worktrace.worktracebackend.timeentry.domain.model.LastTimeEntries;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

/**
 * Adaptador de salida JPA para las consultas de fichajes del dominio de
 * fichajes.
 * <p>
 * Implementa el {@link TimeEntryQueryPort} del panel de fichajes, traduciendo las
 * entidades JPA a los modelos de dominio y manteniendo todas las consultas como
 * solo lectura. Las consultas del cierre diario viven en
 * {@link TimeEntryClosureJpaAdapter}, de modo que cada adaptador sirve a un único
 * bounded context.
 */
@Component
@Transactional(readOnly = true)
public class TimeEntryJpaAdapter implements TimeEntryQueryPort {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryJpaAdapter(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
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
                .map(p -> new DailyEntryCount(p.getEntryDate(), p.getEntryCount()))
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
        return timeEntryRepository.findActiveWithEmployeeByCompany(companyId)
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
     * tiene hora de fin. El resultado se ordena cronológicamente inverso y se
     * limita a 5 eventos.
     */
    @Override
    public List<LastTimeEntries> findTop5ByEmployee(UUID userId) {
        List<TimeEntry> timeEntries = timeEntryRepository.findTop5ByEmployee_UserIdOrderByStartAtDesc(userId);
        return timeEntries.stream()
                .flatMap(this::toEvents)
                .sorted(Comparator.comparing(LastTimeEntries::date).reversed())
                .limit(5)
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

    /**
     * {@inheritDoc}
     * <p>
     * Recupera los fichajes del empleado en la fecha, los descompone en eventos
     * de entrada/salida y los ordena cronológicamente de forma descendente.
     */
    @Override
    public List<LastTimeEntries> findEventsByEmployeeAndDate(UUID userId, LocalDate date) {
        List<TimeEntry> timeEntries = timeEntryRepository.findTimeEntriesByEmployee_UserIdAndWorkDate(userId, date);
        return timeEntries.stream()
                .flatMap(this::toEvents)
                .sorted(Comparator.comparing(LastTimeEntries::date).reversed())
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Suma, a nivel de persistencia, los minutos trabajados por el empleado en el
     * rango de fechas.
     */
    @Override
    public long getWorkedMinutesByEmployeeAndDateRange(UUID userId, LocalDate start, LocalDate end) {
        return timeEntryRepository.getWorkedMinutesByEmployeeAndDateRange(userId, start, end);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delega en la consulta agregada de la base de datos, que agrupa los minutos
     * trabajados por día, y traduce cada fila a {@link DailyWorkedMinutes}.
     */
    @Override
    public List<DailyWorkedMinutes> getDailyWorkedMinutesInRange(UUID userId, LocalDate start, LocalDate end) {
        return timeEntryRepository.getGroupedDailyStatistics(userId, start, end)
                .stream()
                .map(ds-> new DailyWorkedMinutes(
                        ds.getDate(),ds.getWorkedMinutes()
                )).toList();
    }

    /**
     * Descompone un fichaje en sus eventos: siempre una "Entrada" y, si ya tiene
     * hora de fin, también una "Salida".
     *
     * @param te Entidad de fichaje.
     * @return Un flujo con uno o dos {@link LastTimeEntries}.
     */
    private Stream<LastTimeEntries> toEvents(TimeEntry te) {
        Stream.Builder<LastTimeEntries> events = Stream.builder();
        events.add(new LastTimeEntries(te.getId(), "Entrada", te.getStartAt()));
        if (te.getEndAt() != null) {
            events.add(new LastTimeEntries(te.getId(), "Salida", te.getEndAt()));
        }
        return events.build();
    }

}
