package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureNotFoundException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.HashResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.IntegrityResult;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.VerifyIntegrityUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        List<AuditedChange> changes = auditQueryPort.getChangesAfterClosure(companyId, date, record.computedAt());
        if (changes.isEmpty()) {
            return IntegrityResult.CORRUPTED;
        }

        // Reconstruct the snapshot set as it was at closure time:
        // - Start from current (non-deleted) entries.
        // - For each audited change, restore the entry to its pre-edit state (old_data).
        //   ADMIN_ADJUST: replaces the current modified value.
        //   SOFT_DELETE: adds back the entry that was removed after closure.
        // Only the first (oldest) audit per entry is used; it holds the state nearest to closure.
        Map<UUID, TimeEntrySnapshot> reconstructed = new LinkedHashMap<>();
        currentSnapshots.forEach(s -> reconstructed.put(s.id(), s));
        changes.forEach(c -> reconstructed.put(c.timeEntryId(), c.oldSnapshot()));

        Stream<TimeEntrySnapshot> sortedReconstruction = reconstructed.values().stream()
                .sorted(Comparator.comparing(TimeEntrySnapshot::startAt)
                        .thenComparing(TimeEntrySnapshot::id));
        HashResult reconstructedResult = dailyHashChain.compute(sortedReconstruction, record.prevDayHash());

        if (reconstructedResult.hash().equals(record.dayHash())) {
            return IntegrityResult.MODIFIED;
        }
        return IntegrityResult.TAMPERED;
    }
}
