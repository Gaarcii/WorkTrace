package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.DailyClosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyClosureRepository extends JpaRepository<DailyClosure, LocalDate> {
    boolean existsByCompanyIdAndWorkDate(UUID companyId, LocalDate workDate);

    Optional<DailyClosure> findByCompanyIdAndWorkDate(UUID companyId, LocalDate workDate);

    Optional<DailyClosure> findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(UUID companyId, LocalDate workDate);
}
