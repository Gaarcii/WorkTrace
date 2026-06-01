package com.worktrace.worktracebackend.dailyclosure.domain.model;

import com.worktrace.worktracebackend.model.TimeEntryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TimeEntrySnapshot(
        UUID id,
        UUID employeeId,
        LocalDate workDate,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BigDecimal startLat,
        BigDecimal startLng,
        BigDecimal endLat,
        BigDecimal endLng,
        Integer startAccuracyM,
        Integer endAccuracyM,
        String startIp,
        String endIp,
        String startUserAgent,
        String endUserAgent,
        Map<String, Object> startGeoip,
        Map<String, Object> endGeoip,
        List<String> flags,
        TimeEntryStatus timeEntryStatus,
        OffsetDateTime deletedAt,
        UUID deletedBy,
        String deleteReason,
        OffsetDateTime createdAt,
        UUID createdBy,
        OffsetDateTime updatedAt,
        String modificationReason,
        UUID companyId
) {

}
