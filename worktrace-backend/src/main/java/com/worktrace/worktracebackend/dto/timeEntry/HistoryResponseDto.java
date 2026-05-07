package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HistoryResponseDto {
    private Long weeklyWorkedMinutes;
    private Long weeklyTargetMinutes;
    private List<LastTimeEntriesResponseDto> dailyRecords;
    private Long dailyWorkedMinutes;
    private Long dailyTargetMinutes;

}
