package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record DailyClosureRecord(
        UUID companyId,
        LocalDate workDate,
        String dayHash,
        String prevDayHash,
        int recordsCount,
        OffsetDateTime computedAt
) {
    public DailyClosureRecord {
        Objects.requireNonNull(companyId, "El companyId no puede ser nulo");
        Objects.requireNonNull(workDate, "La fecha de trabajo es obligatoria");
        Objects.requireNonNull(dayHash, "El hash del día no puede estar vacío");
        Objects.requireNonNull(computedAt, "La fecha de computado es obligatoria");
    }


}
