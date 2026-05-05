package com.worktrace.worktracebackend.dto.inspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectorIncidenceDto {
    private String name;
    private String email;
    private String image;
    private String incidenceType;
    private OffsetDateTime dateTime;
    private String status;
    private String comment;
    private String resolvedBy;
}
