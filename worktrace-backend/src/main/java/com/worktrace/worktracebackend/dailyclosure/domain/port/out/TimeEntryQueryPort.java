package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeEntryQueryPort {
    long countOpenShifts(UUID companyId, LocalDate date);

    List<TimeEntrySnapshot> findOrderedForClosure(UUID companyId, LocalDate date);
}
