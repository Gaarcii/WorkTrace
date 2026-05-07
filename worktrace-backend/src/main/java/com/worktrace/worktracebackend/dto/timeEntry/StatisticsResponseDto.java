package com.worktrace.worktracebackend.dto.timeEntry;

import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticsResponseDto {
    private long totalWorkedMinutes;
    private long minutesBalance;
    private int incompleteWorkdays;
    private int incidencesCount;
    private List<DailyStatisticDto> dailySummary;
    private List<WorkerIncidenceResponseDto> incidenceList;
}
