package com.worktrace.worktracebackend.dto.workSchedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkScheduleResponseDto {
    private String lugar;
    private String ubicacion;
    private DayOfWeek diaSemana;
    private LocalTime start;
    private LocalTime end;
    private Long horas;
}
