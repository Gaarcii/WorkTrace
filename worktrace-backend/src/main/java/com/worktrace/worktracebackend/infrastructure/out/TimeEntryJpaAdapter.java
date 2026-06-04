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

@Component
@Transactional(readOnly = true)
public class TimeEntryJpaAdapter implements TimeEntryQueryPort, com.worktrace.worktracebackend.timeentry.domain.port.out.TimeEntryQueryPort {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryJpaAdapter(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public long countOpenShifts(UUID companyId, LocalDate targetDate) {
        return timeEntryRepository.countByCompanyIdAndWorkDateAndTimeEntryStatus(
                companyId, targetDate, TimeEntryStatus.OPEN);
    }

    @Override
    public Stream<TimeEntrySnapshot> findOrderedForClosure(UUID companyId, LocalDate date) {
        return timeEntryRepository
                .streamForClosure(companyId, date)
                .map(this::toSnapshot);
    }

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

    @Override
    public Long countByCompanyAndDate(UUID companyId, LocalDate date) {
        return timeEntryRepository.countByCompany_IdAndWorkDate(companyId, date);
    }

    @Override
    public Long getWorkedMinutesByCompanyAndDate(UUID companyId, LocalDate date) {
        return timeEntryRepository.getWorkedMinutesByCompanyAndDate(companyId, date);
    }

}
