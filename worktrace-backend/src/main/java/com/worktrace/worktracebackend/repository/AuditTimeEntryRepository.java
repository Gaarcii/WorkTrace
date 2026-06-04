package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditTimeEntryRepository extends JpaRepository<AuditTimeEntry, UUID> {

    long countByCompanyId(UUID companyId);
    @Query(value = """
            SELECT a.time_entry_id AS timeEntryId,
                   a.action        AS action,
                   a.old_data      AS oldData
            FROM audit_time_entries a
            WHERE a.company_id  = :companyId
              AND a.created_at  > :closureTime
              AND a.action     IN ('ADMIN_ADJUST', 'SOFT_DELETE')
              AND a.time_entry_id IN (
                  SELECT id FROM time_entries WHERE work_date = :workDate
              )
            ORDER BY a.created_at
            """, nativeQuery = true)
    List<AuditChangeProjection> findChangesAfterClosure(
            @Param("companyId") UUID companyId,
            @Param("workDate") LocalDate workDate,
            @Param("closureTime") OffsetDateTime closureTime);

    interface AuditChangeProjection {
        String getTimeEntryId();
        String getAction();
        String getOldData();
    }

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

    @Query(value = """
            SELECT a.time_entry_id AS timeEntryId,
                   a.action        AS action,
                   a.created_at    AS createdAt
            FROM audit_time_entries a
            WHERE a.company_id  = :companyId
              AND a.created_at  > :closureTime
              AND (
                a.old_data->>'workDate' = :workDate
                OR a.new_data->>'workDate' = :workDate
                OR a.old_data->>'work_date' = :workDate
                OR a.new_data->>'work_date' = :workDate
              )
            ORDER BY a.created_at
            """, nativeQuery = true)
    List<AuditIntegrityProjection> findAllChangesForIntegrityCheck(
            @Param("companyId") UUID companyId,
            @Param("workDate") String workDate,
            @Param("closureTime") OffsetDateTime closureTime);

    interface AuditIntegrityProjection {
        String getTimeEntryId();
        String getAction();
        Object getCreatedAt();  // PostgreSQL timestamp devuelve Instant, no OffsetDateTime
    }
}
