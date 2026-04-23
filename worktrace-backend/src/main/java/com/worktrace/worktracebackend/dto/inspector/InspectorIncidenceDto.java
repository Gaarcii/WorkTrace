package com.worktrace.worktracebackend.dto.inspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorIncidenceDto {
    private String nombre;
    private String correo;
    private String imagen;
    private String tipoIncidencia;
    private OffsetDateTime fechaHora;
    private String estado;
    private String comentario;
    private String resueltaPor;
}
