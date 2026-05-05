package com.worktrace.worktracebackend.dto.inspector;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailDto {
    private UUID id;
    private OffsetDateTime registrationDate;
    private BigDecimal weeklyHours;
    private Boolean isActive;
    private List<WorkScheduleResponseDto> workSchedules;
}