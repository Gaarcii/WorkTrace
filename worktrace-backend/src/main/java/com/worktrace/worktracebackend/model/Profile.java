package com.worktrace.worktracebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "weekly_hours")
    private BigDecimal weeklyHours;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private JobPosition position;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private java.util.List<WorkSchedule> workSchedules;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private java.util.List<TimeEntry> timeEntries;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private java.util.List<Incidence> incidences;
}