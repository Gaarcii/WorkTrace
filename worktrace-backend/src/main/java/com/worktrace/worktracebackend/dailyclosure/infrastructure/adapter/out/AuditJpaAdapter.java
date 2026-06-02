package com.worktrace.worktracebackend.dailyclosure.infrastructure.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worktrace.worktracebackend.dailyclosure.domain.model.AuditedChange;
import com.worktrace.worktracebackend.dailyclosure.domain.model.TimeEntrySnapshot;
import com.worktrace.worktracebackend.dailyclosure.domain.port.out.AuditQueryPort;
import com.worktrace.worktracebackend.model.TimeEntryStatus;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository;
import com.worktrace.worktracebackend.repository.AuditTimeEntryRepository.AuditChangeProjection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        // Keep only the first (oldest) audit per entry — that old_data equals the state at closure time.
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
            return new TimeEntrySnapshot(
                    uuid(n, "id"),
                    uuid(n.path("employee"), "userId"),
                    LocalDate.parse(n.get("workDate").asText()),
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
                    enumVal(n, "timeEntryStatus", TimeEntryStatus.class),
                    offsetDateTime(n, "deletedAt"),
                    uuidOrNull(n.path("deletedBy"), "id"),
                    text(n, "deleteReason"),
                    offsetDateTime(n, "createdAt"),
                    uuid(n.path("createdBy"), "id"),
                    offsetDateTime(n, "updatedAt"),
                    text(n, "modificationReason"),
                    uuid(n.path("company"), "id")
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error parsing audit old_data", e);
        }
    }

    private static UUID uuid(JsonNode parent, String field) {
        return UUID.fromString(parent.get(field).asText());
    }

    private static UUID uuidOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : UUID.fromString(node.asText());
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

    private static <E extends Enum<E>> E enumVal(JsonNode parent, String field, Class<E> cls) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : Enum.valueOf(cls, node.asText());
    }
}
