package com.worktrace.worktracebackend.dto.inspector;

import com.worktrace.worktracebackend.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmpleadoDto {
    private UUID id;
    private String photo;
    private String name;
    private String jobPosition;
    private String email;
    private String phone;
    private String employeeCode;
    private Role role;
}
