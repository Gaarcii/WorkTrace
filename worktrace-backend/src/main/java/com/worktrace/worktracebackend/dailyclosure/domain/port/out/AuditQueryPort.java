package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditRecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditQueryPort {
    List<AuditedChange> getChangesAfterClosure(UUID companyId, LocalDate date, OffsetDateTime closureComputedAt);

    List<AuditRecord> findAllChangesForIntegrityCheck(UUID companyId, LocalDate workDate, OffsetDateTime closureComputedAt);
}
