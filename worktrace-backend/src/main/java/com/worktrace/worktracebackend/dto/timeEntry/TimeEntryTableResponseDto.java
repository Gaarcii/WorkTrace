package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeEntryTableResponseDto {
    private UUID id;
    private LocalDate date;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private BigDecimal latStartAt;
    private BigDecimal lngStartAt;
    private BigDecimal latEndAt;
    private BigDecimal lngEndAt;
    private Long workedHours;
}