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
public class IncidenceResponseDto {

    //Tipo de incidencia
    private String tipoIncidencia;
    //Fecha
    private LocalDate fecha;
    //Hora
    private LocalTime hora;
    //mensaje
    private String comentario;
    //Estado
    private EstadoIncidencia estado;
    //Cuando se ha creado ( no tiene que ver con los otros campos)
    private OffsetDateTime creacion;

}
