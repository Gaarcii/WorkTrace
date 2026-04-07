package com.worktrace.worktracebackend.dto.incidenceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidenceTypeItemDto {
    private UUID id;
    private String name;
}
