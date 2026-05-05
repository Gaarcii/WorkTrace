package com.worktrace.worktracebackend.dto.inspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorDailyClosureDto {
    private LocalDate date;
    private String status;
    private Integer timeEntriesCount;
    private String dayHash;
    private String previousDayHash;
    private OffsetDateTime computedAt;
}
