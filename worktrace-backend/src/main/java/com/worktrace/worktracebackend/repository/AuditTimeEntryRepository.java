package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AuditTimeEntryRepository extends JpaRepository<AuditTimeEntry, UUID> {
    long countByCompanyId(UUID companyId);

    @Query("SELECT COUNT(a) > 0 FROM AuditTimeEntry a " +
            "WHERE a.companyId = :companyId " +
            "AND a.createdAt > :closureTime " +
            "AND a.timeEntryId IN (SELECT t.id FROM TimeEntry t WHERE t.workDate = :workDate)")
    boolean existsEditsAfterClosure(@Param("companyId") UUID companyId,
                                    @Param("workDate") LocalDate workDate,
                                    @Param("closureTime") OffsetDateTime closureTime);

    @Query("SELECT a FROM AuditTimeEntry a WHERE a.companyId = :companyId " +
            "AND (cast(:action as string) IS NULL OR :action = '' OR LOWER(a.action) = LOWER(:action)) " +
            "AND (cast(:startDate as date) IS NULL OR cast(a.createdAt as date) >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR cast(a.createdAt as date) <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditTimeEntry> findFilteredAudits(
            @Param("companyId") UUID companyId,
            @Param("action") String action,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            Pageable pageable
    );
}
