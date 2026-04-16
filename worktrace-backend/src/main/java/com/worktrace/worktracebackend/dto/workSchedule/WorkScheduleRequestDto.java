package com.worktrace.worktracebackend.dto.workSchedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleRequestDto {

    @NotNull(message = "El usuario es obligatorio")
    private UUID employeeId;

    @NotNull(message = "La lista de horarios es obligatoria")
    @Valid
    private List<WorkScheduleDayRequestDto> schedules = new ArrayList<>();
}
