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
    private String place;
    private String location;
    private DayOfWeek dayOfWeek;
    private LocalTime start;
    private LocalTime end;
    private Long hours;
}
