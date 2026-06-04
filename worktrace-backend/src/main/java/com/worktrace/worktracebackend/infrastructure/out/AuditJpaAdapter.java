package com.worktrace.worktracebackend.infrastructure.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditRecord;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository.AuditChangeProjection;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository.AuditIntegrityProjection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Component
@Transactional(readOnly = true)
public class AuditJpaAdapter implements AuditQueryPort {

    private final AuditTimeEntryRepository auditTimeEntryRepository;
    private final ObjectMapper objectMapper;

    public AuditJpaAdapter(AuditTimeEntryRepository auditTimeEntryRepository, ObjectMapper objectMapper) {
        this.auditTimeEntryRepository = auditTimeEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AuditedChange> getChangesAfterClosure(UUID companyId, LocalDate date, OffsetDateTime closureComputedAt) {
        List<AuditChangeProjection> rows =
                auditTimeEntryRepository.findChangesAfterClosure(companyId, date, closureComputedAt);

        Map<UUID, AuditedChange> firstPerEntry = new LinkedHashMap<>();
        for (AuditChangeProjection row : rows) {
            if (row.getOldData() == null) continue;
            UUID entryId = UUID.fromString(row.getTimeEntryId());
            firstPerEntry.putIfAbsent(entryId,
                    new AuditedChange(entryId, row.getAction(), parseOldSnapshot(row.getOldData())));
        }
        return List.copyOf(firstPerEntry.values());
    }

    private TimeEntrySnapshot parseOldSnapshot(String json) {
        try {
            JsonNode n = objectMapper.readTree(json);
            String workDate = text(n, "workDate");
            if (workDate == null) {
                throw new IllegalArgumentException("workDate is required in audit old_data");
            }
            return new TimeEntrySnapshot(
                    uuid(n),
                    uuidFromNode(n.path("employee"), "userId"),
                    LocalDate.parse(workDate),
                    offsetDateTime(n, "startAt"),
                    offsetDateTime(n, "endAt"),
                    bigDecimal(n, "startLat"),
                    bigDecimal(n, "startLng"),
                    bigDecimal(n, "endLat"),
                    bigDecimal(n, "endLng"),
                    intVal(n, "startAccuracyM"),
                    intVal(n, "endAccuracyM"),
                    text(n, "startIp"),
                    text(n, "endIp"),
                    text(n, "startUserAgent"),
                    text(n, "endUserAgent"),
                    toMap(n.path("startGeoip")),
                    toMap(n.path("endGeoip")),
                    toStringList(n.path("flags")),
                    timeEntryStatus(n),
                    offsetDateTime(n, "deletedAt"),
                    uuidFromNodeOrNull(n.path("deletedBy")),
                    text(n, "deleteReason"),
                    offsetDateTime(n, "createdAt"),
                    uuidFromNode(n.path("createdBy")),
                    offsetDateTime(n, "updatedAt"),
                    text(n, "modificationReason"),
                    uuidFromNode(n.path("company"))
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error parsing audit old_data", e);
        }
    }

    private static UUID uuid(JsonNode parent) {
        JsonNode node = parent.path("id");
        if (node.isMissingNode() || node.isNull()) {
            throw new IllegalArgumentException("Required field missing: " + "id");
        }
        return UUID.fromString(node.asText());
    }

    private static UUID uuidFromNode(JsonNode node) {
        return uuidFromNode(node, "id");
    }

    private static UUID uuidFromNode(JsonNode node, String field) {
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            throw new IllegalArgumentException("Required field missing: " + field);
        }
        return UUID.fromString(fieldNode.asText());
    }

    private static UUID uuidFromNodeOrNull(JsonNode node) {
        JsonNode idNode = node.path("id");
        return idNode.isMissingNode() || idNode.isNull() ? null : UUID.fromString(idNode.asText());
    }

    private static TimeEntryStatus timeEntryStatus(JsonNode parent) {
        return enumVal(parent);
    }

    private static OffsetDateTime offsetDateTime(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : OffsetDateTime.parse(node.asText());
    }

    private static BigDecimal bigDecimal(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.decimalValue();
    }

    private static Integer intVal(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.intValue();
    }

    private static String text(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        try {
            return objectMapper.treeToValue(node, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static List<String> toStringList(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        List<String> list = new ArrayList<>();
        node.forEach(item -> list.add(item.asText()));
        return list;
    }

    private static TimeEntryStatus enumVal(JsonNode parent) {
        JsonNode node = parent.path("timeEntryStatus");
        return node.isMissingNode() || node.isNull() ? null : TimeEntryStatus.valueOf(node.asText());
    }

    @Override
    public List<AuditRecord> findAllChangesForIntegrityCheck(UUID companyId, LocalDate workDate, OffsetDateTime closureComputedAt) {
        List<AuditIntegrityProjection> rows =
                auditTimeEntryRepository.findAllChangesForIntegrityCheck(companyId, workDate.toString(), closureComputedAt);

        return rows.stream()
                .map(row -> {
                    OffsetDateTime createdAt = convertToOffsetDateTime(row.getCreatedAt());
                    return new AuditRecord(
                            UUID.fromString(row.getTimeEntryId()),
                            row.getAction(),
                            createdAt
                    );
                })
                .toList();
    }

    private OffsetDateTime convertToOffsetDateTime(Object obj) {
        return switch (obj) {
            case null -> null;
            case OffsetDateTime odt -> odt;
            case java.time.Instant instant -> instant.atOffset(java.time.ZoneOffset.UTC);
            case String str -> OffsetDateTime.parse(str);
            default -> throw new IllegalArgumentException("Cannot convert " + obj.getClass() + " to OffsetDateTime");
        };
    }
}
