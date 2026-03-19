package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.dto.timeEntry.EstadisticaDiariaProjection;
import com.worktrace.worktracebackend.model.Status;
import com.worktrace.worktracebackend.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Optional<TimeEntry> findByEmployee_UserIdAndEndAtIsNullAndStatus(UUID userId, Status status);

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
              AND t.work_date BETWEEN :fechaInicio AND :fechaFin
              AND t.deleted_at IS NULL
            """, nativeQuery = true)
    Long getWorkedMinutesByEmployeeAndDateRange(
            @Param("userId") UUID userId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query(value = """
            SELECT
                t.work_date AS fecha,
                COALESCE(SUM(
                    CASE
                        WHEN t.status = 'CLOSED' THEN EXTRACT(EPOCH FROM (t.end_at - t.start_at)) / 60
                        WHEN t.status = 'OPEN' THEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - t.start_at)) / 60
                        ELSE 0
                    END
                ), 0) AS minutosTrabajados
            FROM time_entries t
            WHERE t.employee_id = :userId
              AND t.work_date BETWEEN :fechaInicio AND :fechaFin
              AND t.deleted_at IS NULL
            GROUP BY t.work_date
            ORDER BY t.work_date
            """, nativeQuery = true)
    List<EstadisticaDiariaProjection> getEstadisticasDiariasAgrupadas(
            @Param("userId") UUID userId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    List<TimeEntry> findTimeEntriesByEmployee_UserIdAndWorkDateBetweenOrderByWorkDateDesc(
            UUID userId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    @Query("SELECT MIN(t.workDate) FROM TimeEntry t WHERE t.employee.userId = :userId")
    LocalDate findFirstWorkDateByEmployee(@Param("userId") UUID userId);
}