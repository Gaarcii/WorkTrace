package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.dto.timeEntry.AdminByDateProjection;
import com.worktrace.worktracebackend.dto.timeEntry.DailyStatisticsProjection;
import com.worktrace.worktracebackend.dto.timeEntry.DailyTimeEntryCountProjection;
import com.worktrace.worktracebackend.dto.timeEntry.TimeEntryRowProjection;
import com.worktrace.worktracebackend.model.TimeEntry;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Optional<TimeEntry> findByEmployee_UserIdAndEndAtIsNullAndTimeEntryStatus(UUID userId, TimeEntryStatus timeEntryStatus);

    List<TimeEntry> findTimeEntriesByEmployee_UserIdAndWorkDate(UUID employeeUserId, LocalDate workDate);
    @Query(value = """
             SELECT COALESCE(SUM(
                 CASE
                     WHEN t.status = 'CLOSED' THEN EXTRACT(EPOCH FROM (t.end_at - t.start_at)) / 60
                     WHEN t.status = 'OPEN' THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - t.start_at)) / 60
                     ELSE 0
                 END
             ), 0)
             FROM time_entries t
             WHERE t.employee_id = :userId
               AND t.work_date = :date
               AND t.deleted_at IS NULL
             """, nativeQuery = true)
    Long getWorkedMinutesByEmployeeAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    List<TimeEntry> findTop5ByEmployee_UserIdOrderByStartAtDesc(UUID userId);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE
                            WHEN t.status = 'CLOSED' THEN EXTRACT(EPOCH FROM (t.end_at - t.start_at)) / 60
                            WHEN t.status = 'OPEN' THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - t.start_at)) / 60
                    ELSE 0
                END
            ), 0)
            FROM time_entries t
            WHERE t.employee_id = :userId
              AND t.work_date BETWEEN :startDate AND :endDate
              AND t.deleted_at IS NULL
            """, nativeQuery = true)
    Long getWorkedMinutesByEmployeeAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
            
            SELECT
               t.work_date AS date,
               CAST(
                   COALESCE(SUM(
                       CASE
                           WHEN t.status = 'CLOSED' THEN EXTRACT(EPOCH FROM (t.end_at - t.start_at)) / 60
                           WHEN t.status = 'OPEN' THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - t.start_at)) / 60
                           ELSE 0
                       END
                   ), 0) AS BIGINT
               ) AS workedMinutes
            FROM time_entries t
            WHERE t.employee_id = :userId
              AND t.work_date BETWEEN :startDate AND :endDate
              AND t.deleted_at IS NULL
            GROUP BY t.work_date
            ORDER BY t.work_date
            """, nativeQuery = true)
    List<DailyStatisticsProjection> getGroupedDailyStatistics(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<TimeEntry> findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT MIN(t.workDate) FROM TimeEntry t WHERE t.employee.userId = :userId")
    LocalDate findFirstWorkDateByEmployee(@Param("userId") UUID userId);

    @Query("""
            SELECT t FROM TimeEntry t
            JOIN FETCH t.employee e
            LEFT JOIN FETCH e.position
            WHERE t.company.id = :companyId AND t.endAt IS NULL
            """)
    List<TimeEntry> findActiveWithEmployeeByCompany(@Param("companyId") UUID companyId);

    @Query("""
            SELECT COUNT(DISTINCT t.employee.userId)
            FROM TimeEntry t
            WHERE t.company.id = :companyId
              AND t.workDate = :workDate
              AND t.endAt IS NULL
              AND t.timeEntryStatus = :timeEntryStatus
            """)
    long countDistinctActiveWorkersByCompanyAndWorkDate(
            @Param("companyId") UUID companyId,
            @Param("workDate") LocalDate workDate,
            @Param("timeEntryStatus") TimeEntryStatus timeEntryStatus
    );

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE
                    WHEN t.status = 'CLOSED' THEN EXTRACT(EPOCH FROM (t.end_at - t.start_at)) / 60
                    WHEN t.status = 'OPEN' THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - t.start_at)) / 60
                    ELSE 0
                END
            ), 0)
            FROM time_entries t
            WHERE t.company_id = :companyId
              AND t.work_date = :date
              AND t.deleted_at IS NULL
            """, nativeQuery = true)
    Long getWorkedMinutesByCompanyAndDate(
            @Param("companyId") UUID companyId,
            @Param("date") LocalDate date
    );

    Long countByCompany_IdAndWorkDate(UUID companyId, LocalDate workDate);

    @Query(value = """
            SELECT t.work_date AS entryDate, COUNT(t.id) AS entryCount
            FROM time_entries t
            WHERE t.company_id = :companyId
              AND t.work_date BETWEEN :startDate AND :endDate
              AND t.deleted_at IS NULL
            GROUP BY t.work_date
            ORDER BY t.work_date
            """, nativeQuery = true)
    List<DailyTimeEntryCountProjection> getTimeEntryCountByCompanyAndDateRange(
            @Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Page<TimeEntryRowProjection> findByCompany_IdAndEmployee_UserIdAndDeletedAtIsNullOrderByWorkDateDescStartAtDescIdDesc(
            UUID companyId, UUID employeeId, Pageable pageable);

    @Query(value = """
            SELECT
              t.id AS id,
              e.userId AS employeeId,
              e.fullName AS workerName,
              pos.title AS jobPosition,
              e.avatarUrl AS avatarUrl,
              t.workDate AS date,
              t.startAt AS startAt,
              t.endAt AS endAt
            FROM TimeEntry t
            JOIN t.employee e
            LEFT JOIN e.position pos
            WHERE t.company.id = :companyId AND t.workDate = :workDate
            ORDER BY t.startAt DESC, t.id
            """,
            countQuery = """
                    SELECT COUNT(t) FROM TimeEntry t
                    WHERE t.company.id = :companyId AND t.workDate = :workDate
                    """)
    Page<AdminByDateProjection> findByCompanyIdAndWorkDate(
            @Param("companyId") UUID companyId,
            @Param("workDate") LocalDate workDate,
            Pageable pageable
    );

    List<TimeEntry> findByCompany_IdAndWorkDateBetweenOrderByWorkDateDesc(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT t FROM TimeEntry t
            JOIN FETCH t.employee
            WHERE t.company.id = :companyId AND t.workDate = :date
            ORDER BY t.startAt ASC, t.id ASC
            """)
    @QueryHints(value = @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "100"))
    Stream<TimeEntry> streamForClosure(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

    long countByCompanyIdAndWorkDateAndTimeEntryStatus(UUID companyId, LocalDate workDate, TimeEntryStatus timeEntryStatus);
}