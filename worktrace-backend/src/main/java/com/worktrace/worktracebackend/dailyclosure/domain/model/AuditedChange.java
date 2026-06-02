package com.worktrace.worktracebackend.dailyclosure.domain.model;

import java.util.UUID;

public record AuditedChange(
        UUID timeEntryId,
        String action,
        TimeEntrySnapshot oldSnapshot
) {}
