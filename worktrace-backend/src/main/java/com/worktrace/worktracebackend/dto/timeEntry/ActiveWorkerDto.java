package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiveWorkerDto {
    private UUID employeeId;
    private String fullName;
    private String jobPosition;
    private String avatarUrl;
    private OffsetDateTime timeEntryTime;
    private Long punctuality;
}
