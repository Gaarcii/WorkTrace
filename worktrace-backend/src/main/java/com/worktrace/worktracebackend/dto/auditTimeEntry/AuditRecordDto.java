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
    private OffsetDateTime editionDate;
    private String author;
    private String justification;
    private String changePerformed;
    private UUID referenciaRegistro;
}