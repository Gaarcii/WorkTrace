package com.worktrace.worktracebackend.shared.model;

import java.util.UUID;

public record AuthenticatedUser(
        UUID companyId,
        UUID profileUserId,
        UUID userId
) {
}
