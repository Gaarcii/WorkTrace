package com.worktrace.worktracebackend.dto.timeEntry;

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
public class AdminFichajeDiaResponseDto {
    private UUID id;
    private UUID trabajadorId;
    private String nombreTrabajador;
    private String puestoTrabajo;
    private String avatarUrl;
    private LocalDate fecha;
    private OffsetDateTime entrada;
    private OffsetDateTime salida;
    private Long minutosTrabajados;
}
