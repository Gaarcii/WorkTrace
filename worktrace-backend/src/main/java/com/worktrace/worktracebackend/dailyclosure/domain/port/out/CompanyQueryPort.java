package com.worktrace.worktracebackend.dailyclosure.domain.port.out;

import java.util.List;
import java.util.UUID;

public interface CompanyQueryPort {
    List<UUID> findAllCompanyIds();
}
