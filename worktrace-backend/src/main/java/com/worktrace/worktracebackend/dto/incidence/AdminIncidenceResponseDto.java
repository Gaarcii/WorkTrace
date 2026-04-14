package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.EstadoIncidencia;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminIncidenceResponseDto {
    private UUID id;
    private String nombreTrabajador;
    private String puestoTrabajo;
    private String tipoIncidencia;
    private String comentario;
    private EstadoIncidencia estado;
    private LocalDate fechaAfectada;
    private OffsetDateTime creacion;
    private String avatarUrl;
    private String adminResponse;
}