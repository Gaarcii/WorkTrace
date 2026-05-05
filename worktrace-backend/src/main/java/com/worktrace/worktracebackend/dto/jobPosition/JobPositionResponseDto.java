package com.worktrace.worktracebackend.dto.jobPosition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobPositionResponseDto {
    private UUID id;
    private String name;
}
