package com.worktrace.worktracebackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geoip_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> geoipData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}