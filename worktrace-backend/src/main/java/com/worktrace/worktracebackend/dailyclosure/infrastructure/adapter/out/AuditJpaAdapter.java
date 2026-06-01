package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.out;

import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class AuditJpaAdapter implements AuditQueryPort {

    private final AuditTimeEntryRepository auditTimeEntryRepository;


    public AuditJpaAdapter(AuditTimeEntryRepository auditTimeEntryRepository) {
        this.auditTimeEntryRepository = auditTimeEntryRepository;
    }

    @Override
    public boolean hasEditsAfterClosure(UUID companyId, LocalDate date, OffsetDateTime closureComputedAt) {
        return auditTimeEntryRepository.existsEditsAfterClosure
                (companyId, date, closureComputedAt);
    }
}
