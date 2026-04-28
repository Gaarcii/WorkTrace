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
    private LocalDate fecha;
    private String estado;
    private Integer numeroFichajes;
    private String hashDelDia;
    private String hashDiaAnterior;
    private OffsetDateTime computado;
}
