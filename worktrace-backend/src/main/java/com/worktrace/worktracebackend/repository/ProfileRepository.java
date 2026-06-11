package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("SELECT p.weeklyHours FROM Profile p WHERE p.user.id = :userId")
    Optional<BigDecimal> findWeeklyHoursByUserId(@Param("userId") UUID userId);

    Page<Profile> findByUser_Company_IdAndUser_Role(UUID companyId, Role role, Pageable pageable);
}