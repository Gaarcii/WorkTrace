package com.worktrace.worktracebackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {
    private UUID id;
    private String nombre;
    private String dni;
    private String email;
    private String puesto;
    private String horasSemanales;
    private String estado;
    private OffsetDateTime fechaAlta;
}
