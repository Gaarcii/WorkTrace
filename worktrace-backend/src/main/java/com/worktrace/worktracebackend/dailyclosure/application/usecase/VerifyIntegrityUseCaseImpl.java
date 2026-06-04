package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.*;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class VerifyIntegrityUseCaseImpl implements VerifyIntegrityUseCase {

    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;
    private final AuditQueryPort auditQueryPort;

    public VerifyIntegrityUseCaseImpl(
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort,
            AuditQueryPort auditQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain();
        this.auditQueryPort = auditQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrityResult execute(UUID companyId, LocalDate date) {
        DailyClosureRecord record = dailyClosurePort.findByDate(companyId, date)
                .orElseThrow(() -> new DailyClosureNotFoundException(date));

        List<TimeEntrySnapshot> currentSnapshots;
        try (Stream<TimeEntrySnapshot> stream = timeEntryQueryPort.findOrderedForClosure(companyId, date)) {
            currentSnapshots = stream.toList();
        }
        HashResult result = dailyHashChain.compute(currentSnapshots.stream(), record.prevDayHash());

        if (result.hash().equals(record.dayHash())) {
            return IntegrityResult.VALID;
        }

        List<AuditRecord> auditRecords = auditQueryPort.findAllChangesForIntegrityCheck(companyId, date, record.computedAt());
        if (auditRecords.isEmpty()) {
            return IntegrityResult.CORRUPTED;
        }

        boolean hasDirectModify = auditRecords.stream()
                .anyMatch(audit -> "DB_DIRECT_MODIFY".equals(audit.action()));

        if (hasDirectModify) {
            return IntegrityResult.CORRUPTED;
        }

        return IntegrityResult.MODIFIED;
    }
}
