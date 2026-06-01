package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AuditQueryPort {
    boolean hasEditsAfterClosure(UUID companyId, LocalDate date, OffsetDateTime closureComputedAt);
}
