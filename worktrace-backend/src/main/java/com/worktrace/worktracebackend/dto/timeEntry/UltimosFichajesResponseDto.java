package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UltimosFichajesResponseDto {
    private UUID timeEntryId;
    private String tipoEvento;
    private OffsetDateTime fecha;
}