package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.IncidenceStatus;
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

    long countByCompany_IdAndStatus(UUID companyId, IncidenceStatus status);

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

    Page<Incidence> findByCompany_IdAndStatus(UUID companyId, IncidenceStatus status, Pageable pageable);

    Page<Incidence> findByCompany_IdAndStatusIn(UUID company_id, Collection<IncidenceStatus> status, Pageable pageable);

    boolean existsByType_IdAndCompany_Id(UUID typeId, UUID companyId);

    @Query("SELECT i FROM Incidence i " +
            "WHERE i.company.id = :companyId " +
            "AND (:estado IS NULL OR i.status = :estado) " +
            "AND (cast(:tipoIncidenciaId as uuid) IS NULL OR i.type.id = :tipoIncidenciaId) " +
            "AND (cast(:busqueda as string) IS NULL OR LOWER(i.profile.fullName) " +
            "LIKE LOWER(CONCAT('%', cast(:busqueda as string), '%')))")
    Page<Incidence> findFilteredIncidences(
            @Param("companyId") UUID companyId,
            @Param("estado") IncidenceStatus estado,
            @Param("tipoIncidenciaId") UUID tipoIncidenciaId,
            @Param("busqueda") String busqueda,
            Pageable pageable
    );
}
