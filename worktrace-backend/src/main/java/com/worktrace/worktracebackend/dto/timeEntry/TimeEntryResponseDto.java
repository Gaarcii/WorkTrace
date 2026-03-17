package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TimeEntryResponseDto {
    private UUID id;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String status;
}