package com.worktrace.worktracebackend.dto.auditTimeEntry;

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
public class AuditRecordDto {
    private OffsetDateTime fechaEdicion;
    private String autor;
    private String justificacion;
    private String cambioRealizado;
    private UUID referenciaRegistro;
}