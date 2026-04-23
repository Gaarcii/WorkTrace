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
    private long totalEmpleados;
    private long totalIncidencias;
    private long trabajadoresActivosHoy;
    private long totalAuditLogs;
}

