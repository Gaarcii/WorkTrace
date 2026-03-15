package com.worktrace.worktracebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "known_ips")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnownIp {
    @Id
    @Column(name = "ip", nullable = false, updatable = false, columnDefinition = "inet")
    private String ip;

    @Column(name = "geoip_data", nullable = false, columnDefinition = "jsonb")
    private String geoipData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
