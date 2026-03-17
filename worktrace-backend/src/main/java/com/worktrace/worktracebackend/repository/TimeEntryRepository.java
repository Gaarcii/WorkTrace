package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.Status;
import com.worktrace.worktracebackend.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Optional<TimeEntry> findByEmployee_UserIdAndEndAtIsNullAndStatus(UUID userId, Status status);
}
