package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.dto.incidenceType.IncidenceTypeProjection;
import com.worktrace.worktracebackend.model.IncidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenceTypeRepository extends JpaRepository<IncidenceType, UUID> {
    List<IncidenceTypeProjection> findByCompany_Id(UUID companyId);
}
