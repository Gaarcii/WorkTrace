package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeProjection;
import com.worktrace.worktracebackend.model.IncidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidenceTypeRepository extends JpaRepository<IncidenceType, UUID> {
    List<IncidenceTypeProjection> findByCompany_IdAndDeletedAtIsNull(UUID companyId);

    Optional<IncidenceType> findByIdAndCompany_IdAndDeletedAtIsNull(UUID id, UUID companyId);

    Optional<IncidenceType> findByNameIgnoreCaseAndCompany_Id(String name, UUID companyId);

}
