package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.WorkSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkSiteRepository extends JpaRepository<WorkSite, UUID> {
    List<WorkSite> findByCompany_Id(UUID companyId);
    Optional<WorkSite> findByIdAndCompany_Id(UUID id, UUID companyId);
}
