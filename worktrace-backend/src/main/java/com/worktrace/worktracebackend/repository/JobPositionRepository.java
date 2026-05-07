package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, UUID> {

    List<JobPosition> findByCompany_Id(UUID companyId);

    java.util.Optional<JobPosition> findByTitleIgnoreCaseAndCompany_Id(String title, UUID companyId);
}
