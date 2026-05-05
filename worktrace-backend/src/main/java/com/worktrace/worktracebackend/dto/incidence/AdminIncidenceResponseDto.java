package com.worktrace.worktracebackend.dto.incidence;

import com.worktrace.worktracebackend.model.IncidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminIncidenceResponseDto {
    private UUID id;
    private String employeeName;
    private String jobPosition;
    private String incidenceType;
    private String comment;
    private IncidenceStatus status;
    private LocalDate affectedDate;
    private OffsetDateTime createdAt;
    private String avatarUrl;
    private String adminResponse;
}
