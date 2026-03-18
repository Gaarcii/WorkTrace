package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ResumenDiarioResponseDto {

    private long minutosAcumulados;
    private long minutosObjetivo;
    private OffsetDateTime horaEntrada;
}
