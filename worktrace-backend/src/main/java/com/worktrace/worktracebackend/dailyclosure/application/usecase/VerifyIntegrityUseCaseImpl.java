package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class VerifyIntegrityUseCaseImpl implements VerifyIntegrityUseCase {
    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;
    private final AuditQueryPort auditQueryPort;

    public VerifyIntegrityUseCaseImpl(
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort,
            HashPort hashPort ,
            AuditQueryPort auditQueryPort) {
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain(hashPort);
        this.auditQueryPort = auditQueryPort;
    }


    @Override
    @Transactional
    public IntegrityResult execute(UUID companyId, LocalDate date) {
        DailyClosureRecord dailyHash = dailyClosurePort.findByDate(companyId, date)
                .orElseThrow(() -> new DailyClosureNotFoundException(date));

        List<TimeEntrySnapshot> snapshots = timeEntryQueryPort.findOrderedForClosure(companyId, date);

        String currentHash = dailyHashChain.compute(snapshots, dailyHash.prevDayHash());

        if (currentHash.equals(dailyHash.dayHash())) {
            return IntegrityResult.VALID;
        } else if (auditQueryPort.hasEditsAfterClosure(companyId, date, dailyHash.computedAt())) {
            return IntegrityResult.MODIFIED;
        } else {
            return IntegrityResult.CORRUPTED;
        }
    }
}