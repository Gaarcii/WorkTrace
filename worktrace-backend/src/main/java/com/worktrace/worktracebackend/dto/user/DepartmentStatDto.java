package com.worktrace.worktracebackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatDto {
    private String Departamento;
    private Long TotalTrabajadores;
    private Long TrabajadoresActivos;

}
