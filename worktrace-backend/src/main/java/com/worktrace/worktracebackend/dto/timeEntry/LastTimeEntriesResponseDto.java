package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LastTimeEntriesResponseDto {
    private UUID timeEntryId;
    private String eventType;
    private OffsetDateTime date;
}