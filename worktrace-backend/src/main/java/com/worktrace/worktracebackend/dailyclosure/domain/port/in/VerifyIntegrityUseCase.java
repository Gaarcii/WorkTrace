package com.worktrace.worktracebackend.dailyclosure.domain.port.in;

import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;

import java.time.LocalDate;
import java.util.UUID;

public interface VerifyIntegrityUseCase {
    IntegrityResult execute(UUID companyId, LocalDate date);
}
