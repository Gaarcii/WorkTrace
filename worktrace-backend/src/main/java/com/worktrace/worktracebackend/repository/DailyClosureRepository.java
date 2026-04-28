package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.DailyClosure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyClosureRepository extends JpaRepository<DailyClosure, LocalDate> {
    boolean existsByCompanyIdAndWorkDate(UUID companyId, LocalDate workDate);

    Optional<DailyClosure> findByCompanyIdAndWorkDate(UUID companyId, LocalDate workDate);

    Optional<DailyClosure> findTopByCompanyIdAndWorkDateLessThanOrderByWorkDateDesc(UUID companyId, LocalDate workDate);

    @Query("SELECT d FROM DailyClosure d WHERE d.company.id = :companyId " +
           "AND (cast(:startDate as date) IS NULL OR d.workDate >= :startDate) " +
           "AND (cast(:endDate as date) IS NULL OR d.workDate <= :endDate) " +
           "ORDER BY d.workDate DESC")
    Page<DailyClosure> findFilteredClosures(
            @Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
