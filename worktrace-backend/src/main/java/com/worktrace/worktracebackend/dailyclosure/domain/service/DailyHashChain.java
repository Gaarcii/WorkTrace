package com.worktrace.worktracebackend.dailyclosure.domain.service;

import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.HashPort;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class DailyHashChain {
    private static final String HASH_VERSION_V2 = "V2";
    private static final String HASH_SEPARATOR = ":";

    private final HashPort hashPort;

    public DailyHashChain(HashPort hashPort) {
        this.hashPort = hashPort;
    }

    public String compute(List<TimeEntrySnapshot> snapshots, String prevHash) {
        StringBuilder rawData = new StringBuilder();
        if (snapshots.isEmpty()) {
            rawData.append("NO_ACTIVITY");
        } else {
            for (TimeEntrySnapshot snapshot : snapshots) {
                rawData.append(snapshot.id())
                        .append(snapshot.employeeId())
                        .append(snapshot.workDate())
                        .append(snapshot.startAt() != null ? snapshot.startAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(snapshot.endAt() != null ? snapshot.endAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(snapshot.startLat())
                        .append(snapshot.startLng())
                        .append(snapshot.endLat() != null ? snapshot.endLat() : "NULL")
                        .append(snapshot.endLng() != null ? snapshot.endLng() : "NULL")
                        .append(snapshot.startAccuracyM())
                        .append(snapshot.endAccuracyM() != null ? snapshot.endAccuracyM() : "NULL")
                        .append(snapshot.startIp())
                        .append(snapshot.endIp() != null ? snapshot.endIp() : "NULL")
                        .append(snapshot.startUserAgent())
                        .append(snapshot.endUserAgent() != null ? snapshot.endUserAgent() : "NULL")
                        .append(snapshot.startGeoip())
                        .append(snapshot.endGeoip() != null ? snapshot.endGeoip() : "NULL")
                        .append(snapshot.flags() != null ? snapshot.flags() : "NULL")
                        .append(snapshot.timeEntryStatus())
                        .append(snapshot.deletedAt() != null ? snapshot.deletedAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(snapshot.deletedBy() != null ? snapshot.deletedBy() : "NULL").append(snapshot.deleteReason() != null ? snapshot.deleteReason() : "NULL")
                        .append(snapshot.createdAt() != null ? snapshot.createdAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(snapshot.createdBy() != null ? snapshot.createdBy() : "NULL").append(snapshot.updatedAt() != null ? snapshot.updatedAt().truncatedTo(ChronoUnit.SECONDS) : "NULL")
                        .append(snapshot.modificationReason() != null ? snapshot.modificationReason() : "NULL")
                        .append(snapshot.companyId() != null ? snapshot.companyId() : "NULL");
            }
        }

        String concatenatedData = prevHash + rawData;
        return HASH_VERSION_V2 + HASH_SEPARATOR + hashPort.sha256Hex(concatenatedData);
    }

}
