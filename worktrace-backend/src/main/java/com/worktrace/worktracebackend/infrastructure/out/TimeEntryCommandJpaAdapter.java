package com.worktrace.worktracebackend.infrastructure.out;

import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.ProfileRepository;
import com.worktrace.worktracebackend.repository.TimeEntryRepository;
import com.worktrace.worktracebackend.repository.UserRepository;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventCommand;
import com.worktrace.worktracebackend.timeentry.domain.model.ClockEventResult;
import com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryCommandPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Adaptador de salida JPA para las operaciones de escritura de fichajes.
 * <p>
 * Implementa {@link TimeEntryCommandPort} traduciendo las órdenes de dominio a
 * mutaciones sobre la entidad JPA {@link TimeEntry}. Resuelve las asociaciones
 * (trabajador, empresa y autor) mediante referencias perezosas para no cargar
 * las entidades completas, y resuelve dentro de una única transacción si el
 * evento abre o cierra la jornada del trabajador.
 */
@Component
public class TimeEntryCommandJpaAdapter implements TimeEntryCommandPort {

    private final TimeEntryRepository timeEntryRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public TimeEntryCommandJpaAdapter(TimeEntryRepository timeEntryRepository,
                                      ProfileRepository profileRepository,
                                      UserRepository userRepository,
                                      CompanyRepository companyRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca la jornada abierta del trabajador; si existe, la cierra con los datos
     * de salida del evento (fusionando las banderas de auditoría); si no, crea un
     * nuevo fichaje abierto. Todo dentro de una misma transacción para garantizar
     * la atomicidad.
     */
    @Override
    @Transactional
    public ClockEventResult registerClockEvent(ClockEventCommand command) {
        Optional<TimeEntry> openTimeEntryOpt = timeEntryRepository
                .findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(command.employeeUserId(), TimeEntryStatus.OPEN);

        TimeEntry savedTimeEntry;

        if (openTimeEntryOpt.isPresent()) {
            TimeEntry openTimeEntry = openTimeEntryOpt.get();

            openTimeEntry.setEndAt(OffsetDateTime.now());
            openTimeEntry.setEndLat(command.lat());
            openTimeEntry.setEndLng(command.lng());
            openTimeEntry.setEndAccuracyM(command.accuracyMeters());
            openTimeEntry.setEndIp(command.realIp());
            openTimeEntry.setEndUserAgent(command.userAgent());
            openTimeEntry.setEndGeoip(command.geoIpMap());

            if (openTimeEntry.getFlags() != null) {
                openTimeEntry.getFlags().addAll(command.flags());
            } else {
                openTimeEntry.setFlags(command.flags());
            }

            openTimeEntry.setTimeEntryStatus(TimeEntryStatus.CLOSED);
            savedTimeEntry = timeEntryRepository.save(openTimeEntry);

        } else {
            TimeEntry newTimeEntry = new TimeEntry();
            newTimeEntry.setEmployee(profileRepository.getReferenceById(command.employeeUserId()));
            newTimeEntry.setCompany(companyRepository.getReferenceById(command.companyId()));
            newTimeEntry.setCreatedBy(userRepository.getReferenceById(command.employeeUserId()));

            newTimeEntry.setWorkDate(LocalDate.now());
            newTimeEntry.setStartAt(OffsetDateTime.now());
            newTimeEntry.setCreatedAt(OffsetDateTime.now());

            newTimeEntry.setStartLat(command.lat());
            newTimeEntry.setStartLng(command.lng());
            newTimeEntry.setStartAccuracyM(command.accuracyMeters());
            newTimeEntry.setStartIp(command.realIp());
            newTimeEntry.setStartUserAgent(command.userAgent());
            newTimeEntry.setStartGeoip(command.geoIpMap());

            newTimeEntry.setFlags(command.flags());
            newTimeEntry.setTimeEntryStatus(TimeEntryStatus.OPEN);

            savedTimeEntry = timeEntryRepository.save(newTimeEntry);
        }

        return new ClockEventResult(
                savedTimeEntry.getId(),
                savedTimeEntry.getStartAt(),
                savedTimeEntry.getEndAt(),
                savedTimeEntry.getTimeEntryStatus().name()
        );
    }
}
