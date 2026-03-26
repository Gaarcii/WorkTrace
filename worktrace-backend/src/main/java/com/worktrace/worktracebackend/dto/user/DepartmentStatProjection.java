package com.worktrace.worktracebackend.dto.user;

public interface DepartmentStatProjection {
    String getDepartamento();

    Long getTotalTrabajadores();

    Long getTrabajadoresActivos();
}
