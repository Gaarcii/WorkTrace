package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.IncidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerIncidenceResponseDto {
    private String tipoIncidencia;
    private LocalDate fecha;
    private LocalTime hora;
    private String comentario;
    private IncidenceStatus estado;
    private OffsetDateTime creacion;

}