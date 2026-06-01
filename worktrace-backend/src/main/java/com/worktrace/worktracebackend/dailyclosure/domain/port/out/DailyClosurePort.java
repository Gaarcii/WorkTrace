package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyClosurePort {
    boolean existsForDate(UUID companyId, LocalDate date);

    Optional<String> findPreviousHash(UUID companyId, LocalDate beforeDate);

    void save(DailyClosureRecord record);

    Optional<DailyClosureRecord> findByDate(UUID companyId, LocalDate date);
}
