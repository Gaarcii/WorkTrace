package com.worktrace.worktracebackend.dto.timeEntry;

import com.worktrace.worktracebackend.dto.incidence.WorkerIncidenceResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticsResponseDto {
    private long minutosTrabajadosTotal;
    private long balanceMinutos;
    private int jornadasIncompletas;
    private int incidencias;
    private List<EstadisticaDiariaDto> resumenDiario;
    private List<WorkerIncidenceResponseDto> incidenciasList;
}
