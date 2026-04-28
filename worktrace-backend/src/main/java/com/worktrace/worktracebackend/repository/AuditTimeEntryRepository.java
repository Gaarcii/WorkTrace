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

    Page<AuditTimeEntry> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
}
