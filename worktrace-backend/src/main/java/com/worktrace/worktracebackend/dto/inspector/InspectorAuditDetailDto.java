package com.worktrace.worktracebackend.dto.inspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorAuditDetailDto {
    private UUID id;
    private OffsetDateTime fechaEliminacion;
    private String motivo;
    private String ejecutador;
    private String datosAntesModificacion;
}
