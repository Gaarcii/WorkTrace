package com.worktrace.worktracebackend.dto.workSchedule;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleRequestDto {

    @NotNull(message = "El usuario es obligatorio")
    private UUID employeeId;

    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime endTime;

    @NotNull(message = "El lugar de trabajo es obligatorio")
    private UUID siteId;
}
