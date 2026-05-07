package com.worktrace.worktracebackend.dto.user;

import com.worktrace.worktracebackend.dto.workSchedule.WorkScheduleResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private String fullName;
    private String avatarUrl;
    private String jobPosition;
    private String email;
    private String phone;
    private List<WorkScheduleResponseDto> schedule;
    private String updatedToken;
}

