package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class DailySummaryResponseDto {

    private long minutosAcumulados;
    private long minutosObjetivo;
    private OffsetDateTime horaEntrada;
    private List<UltimosFichajesResponseDto> ultimosFichajes;
}
