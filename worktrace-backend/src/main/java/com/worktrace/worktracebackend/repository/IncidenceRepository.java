package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenceRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByProfile_UserId(UUID userId);
}
