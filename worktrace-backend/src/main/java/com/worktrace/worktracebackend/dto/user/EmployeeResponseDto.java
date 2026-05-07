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
    private String name;
    private String dni;
    private String email;
    private String avatarUrl;
    private String phone;
    private String jobPosition;
    private String weeklyHours;
    private String status;
    private OffsetDateTime registrationDate;
}
