package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    @Query(value = """
            SELECT COUNT(*)
            FROM incidents
            WHERE user_id = :userId
              AND date BETWEEN :fechaInicio AND :fechaFin
            """, nativeQuery = true)
    int countIncidentsByUsuarioYFechas(
            @Param("userId") UUID userId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}