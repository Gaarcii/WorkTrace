package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.out;

import com.worktrace.worktracebackend.dailyclosure.domain.model.DailyClosureRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.DailyClosurePort;
import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.DailyClosure;
import com.worktrace.worktracebackend.repository.CompanyRepository;
import com.worktrace.worktracebackend.repository.DailyClosureRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class DailyClosureJpaAdapter implements DailyClosurePort {

    private final DailyClosureRepository dailyClosureRepository;
    private final CompanyRepository companyRepository;

    public DailyClosureJpaAdapter(DailyClosureRepository dailyClosureRepository, CompanyRepository companyRepository) {
        this.dailyClosureRepository = dailyClosureRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public boolean existsForDate(UUID companyId, LocalDate date) {
        return dailyClosureRepository.existsByCompanyIdAndWorkDate(companyId, date);
    }

    @Override
    public Optional<String> findPreviousHash(UUID companyId, LocalDate beforeDate) {
        return dailyClosureRepository
                .findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(companyId, beforeDate)
                .map(DailyClosure::getDayHash);
    }

    @Override
    @Transactional
    public void save(DailyClosureRecord record) {

        Company companyProxy = companyRepository.getReferenceById(record.companyId());

        DailyClosure dailyClosure = new DailyClosure();
        dailyClosure.setCompany(companyProxy);
        dailyClosure.setWorkDate(record.workDate());
        dailyClosure.setDayHash(record.dayHash());
        dailyClosure.setPrevDayHash(record.prevDayHash());
        dailyClosure.setRecordsCount(record.recordsCount());
        dailyClosure.setComputedAt(record.computedAt());

        dailyClosureRepository.save(dailyClosure);
    }

    @Override
    public Optional<DailyClosureRecord> findByDate(UUID companyId, LocalDate date) {
        return dailyClosureRepository.findByCompanyIdAndWorkDate(companyId, date)
                .map(this::toRecord);
    }

    private DailyClosureRecord toRecord(DailyClosure jpa) {
        return new DailyClosureRecord(
                jpa.getCompany().getId(),
                jpa.getWorkDate(),
                jpa.getDayHash(),
                jpa.getPrevDayHash(),
                jpa.getRecordsCount(),
                jpa.getComputedAt()
        );
    }
}