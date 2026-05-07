package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminTimeEntryByDateResponseDto {
    private UUID id;
    private UUID employeeId;
    private String workerName;
    private String jobPosition;
    private String avatarUrl;
    private LocalDate date;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private Long workedMinutes;
}
