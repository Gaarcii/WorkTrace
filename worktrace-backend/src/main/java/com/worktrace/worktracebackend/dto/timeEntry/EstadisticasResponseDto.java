package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EstadisticasResponseDto {
    private long minutosTrabajadosTotal;
    private long balanceMinutos;
    private int jornadasIncompletas;
    private int incidencias;
    private List<EstadisticaDiariaDto> resumenDiario;
}