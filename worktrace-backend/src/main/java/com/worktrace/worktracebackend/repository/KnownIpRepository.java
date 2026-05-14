package com.worktrace.worktracebackend.repository;

import com.worktrace.worktracebackend.model.KnownIp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnownIpRepository extends JpaRepository<KnownIp, String> {
    @Query(value = "SELECT * FROM known_ips WHERE ip = CAST(:ip AS inet)", nativeQuery = true)
    Optional<KnownIp> findByIp(@Param("ip") String ip);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO known_ips (ip, geo_ip_data, created_at, company_id) VALUES (CAST(:ip AS inet), CAST(:geoipData AS jsonb), :createdAt, :companyId)", nativeQuery = true)
    void saveNativeIp(@Param("ip") String ip, @Param("geoipData") String geoipData, @Param("createdAt") OffsetDateTime createdAt, @Param("companyId") UUID companyId);
}
