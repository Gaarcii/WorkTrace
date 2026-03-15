package com.worktrace.worktracebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "daily_closures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyClosure {

    @Id
    @Column(name = "work_date", nullable = false, updatable = false)
    private LocalDate workDate;

    @Column(name = "records_count", nullable = false)
    private Integer recordsCount;

    @Column(name = "day_hash", nullable = false)
    private String dayHash;

    @Column(name = "prev_day_hash")
    private String prevDayHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "computed_at", nullable = false)
    private OffsetDateTime computedAt;
}
