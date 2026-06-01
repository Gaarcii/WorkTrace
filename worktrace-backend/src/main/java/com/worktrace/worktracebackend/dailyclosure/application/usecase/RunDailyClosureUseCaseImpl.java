package com.worktrace.worktracebackend.dailyclosure.application.usecase;

import com.worktrace.worktracebackend.dailyclosure.domain.exception.DailyClosureAlreadyExistsException;
import com.worktrace.worktracebackend.dailyclosure.domain.exception.OpenShiftsExistException;
import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.in.RunDailyClosureUseCase;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.CompanyQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.TimeEntryQueryPort;
import com.worktrace.worktracebackend.dailyclosure.domain.service.DailyHashChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RunDailyClosureUseCaseImpl implements RunDailyClosureUseCase {

    private final CompanyQueryPort companyQueryPort;
    private final TimeEntryQueryPort timeEntryQueryPort;
    private final DailyClosurePort dailyClosurePort;
    private final DailyHashChain dailyHashChain;

    public RunDailyClosureUseCaseImpl(
            CompanyQueryPort companyQueryPort,
            TimeEntryQueryPort timeEntryQueryPort,
            DailyClosurePort dailyClosurePort,
            HashPort hashPort) {
        this.companyQueryPort = companyQueryPort;
        this.timeEntryQueryPort = timeEntryQueryPort;
        this.dailyClosurePort = dailyClosurePort;
        this.dailyHashChain = new DailyHashChain(hashPort);
    }

    @Override
    @Transactional
    public void execute(LocalDate targetDate) {
        List<UUID> companyIds = companyQueryPort.findAllCompanyIds();

        for (UUID companyId : companyIds) {
            try {
                if (dailyClosurePort.existsForDate(companyId, targetDate)) {
                    throw new DailyClosureAlreadyExistsException(targetDate);
                }

                long openShifts = timeEntryQueryPort.countOpenShifts(companyId, targetDate);
                if (openShifts > 0) throw new OpenShiftsExistException(openShifts);

                String prevDailyHash = dailyClosurePort.findPreviousHash(companyId, targetDate)
                        .orElse("GENESIS_HASH_0000000000000000000000000000");

                List<TimeEntrySnapshot> snapshots = timeEntryQueryPort.findOrderedForClosure(companyId, targetDate);

                String hash = this.dailyHashChain.compute(snapshots, prevDailyHash);

                DailyClosureRecord dailyClosureRecord = new DailyClosureRecord(
                        companyId,
                        targetDate,
                        hash,
                        prevDailyHash,
                        snapshots.size(),
                        OffsetDateTime.now()
                );

                dailyClosurePort.save(dailyClosureRecord);
            } catch (Exception e) {
                log.error("Fallo en cierre de empresa {}: {}", companyId, e.getMessage());
            }
        }
    }
}
