package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class DailySummaryResponseDto {
    private long accumulatedMinutes;
    private long targetMinutes;
    private OffsetDateTime entryTime;
    private List<LastTimeEntriesResponseDto> lastTimeEntries;
}
