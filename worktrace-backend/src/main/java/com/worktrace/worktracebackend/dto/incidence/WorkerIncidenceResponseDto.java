package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.EstadoIncidencia;
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
    private EstadoIncidencia estado;
    private OffsetDateTime creacion;

}
