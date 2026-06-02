package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

public interface TimeEntryQueryPort {
    long countOpenShifts(UUID companyId, LocalDate date);

    Stream<TimeEntrySnapshot> findOrderedForClosure(UUID companyId, LocalDate date);
}
