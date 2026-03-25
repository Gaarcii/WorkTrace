package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiveWorkerDto {
    private UUID trabajadorId;
    private String nombreCompleto;
    private String puestoTrabajo;
    private String urlAvatar;
    private OffsetDateTime horaFichaje;
    private Long puntualidad;
}
