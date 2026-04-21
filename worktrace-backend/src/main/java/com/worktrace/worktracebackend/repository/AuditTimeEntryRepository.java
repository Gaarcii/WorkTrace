package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.AuditTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditTimeEntryRepository extends JpaRepository<AuditTimeEntry, UUID> {
}