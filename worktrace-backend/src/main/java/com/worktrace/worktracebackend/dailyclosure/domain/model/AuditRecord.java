package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditRecord(
        UUID timeEntryId,
        String action,
        OffsetDateTime createdAt
) {}
