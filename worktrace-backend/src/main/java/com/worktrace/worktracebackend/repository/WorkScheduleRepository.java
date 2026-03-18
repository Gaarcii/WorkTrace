package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
    Optional<WorkSchedule> findByEmployee_UserIdAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek);
}
