package com.worktrace.worktracebackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE time_entries SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Profile employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(name = "start_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal startLat;

    @Column(name = "start_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal startLng;

    @Column(name = "end_lat", precision = 9, scale = 6)
    private BigDecimal endLat;

    @Column(name = "end_lng", precision = 9, scale = 6)
    private BigDecimal endLng;

    @Column(name = "start_accuracy_m", nullable = false)
    private Integer startAccuracyM;

    @Column(name = "end_accuracy_m")
    private Integer endAccuracyM;

    @Column(name = "start_ip", nullable = false, columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String startIp;

    @Column(name = "end_ip", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String endIp;

    @Column(name = "start_user_agent", nullable = false)
    private String startUserAgent;

    @Column(name = "end_user_agent")
    private String endUserAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "start_geoip", columnDefinition = "jsonb")
    private java.util.Map<String, Object> startGeoip;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "end_geoip", columnDefinition = "jsonb")
    private java.util.Map<String, Object> endGeoip;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "flags", columnDefinition = "text[]")
    private java.util.List<String> flags;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EstadoFichaje estadoFichaje;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "modification_reason")
    private String modificationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
