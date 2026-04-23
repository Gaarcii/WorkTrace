package com.worktrace.worktracebackend.dto.inspector;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmpleadoDetalleDto {
    private UUID id;
    private OffsetDateTime registrationDate;
    private BigDecimal weeklyHours;
    private Boolean isActive;
    private List<WorkScheduleResponseDto> workSchedules;
}
