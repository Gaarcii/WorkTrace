package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.EstadoIncidencia;
import com.worktrace.worktracebackend.model.Incidence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenceRepository extends JpaRepository<Incidence, UUID> {
    List<Incidence> findByProfile_UserId(UUID userId);

    long countByCompany_Id(UUID companyId);

    List<Incidence> getIncidencesByProfile_UserIdAndDateBetween(
            UUID profile_userId, LocalDate date, LocalDate date2
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM incidences
            WHERE user_id = :userId
              AND date BETWEEN :fechaInicio AND :fechaFin
            """, nativeQuery = true)
    int countIncidentsByUsuarioYFechas(
            @Param("userId") UUID userId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    Page<Incidence> findByCompany_IdAndStatus(UUID companyId, EstadoIncidencia status, Pageable pageable);

    Page<Incidence> findByCompany_IdAndStatusIn(UUID company_id, Collection<EstadoIncidencia> status, Pageable pageable);

    boolean existsByType_IdAndCompany_Id(UUID typeId, UUID companyId);

}
