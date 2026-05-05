package com.worktrace.worktracebackend.dto.inspector;

import com.worktrace.worktracebackend.model.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private UUID id;
    private String photo;
    private String name;
    private String jobPosition;
    private String email;
    private String phone;
    private String employeeCode;
    private Role role;
}
