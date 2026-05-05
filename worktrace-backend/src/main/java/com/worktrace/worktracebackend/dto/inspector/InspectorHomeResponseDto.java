package com.worktrace.worktracebackend.dto.inspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorHomeResponseDto {
    private long totalEmployees;
    private long totalIncidences;
    private long activeEmployeesToday;
    private long totalAuditLogs;
}

