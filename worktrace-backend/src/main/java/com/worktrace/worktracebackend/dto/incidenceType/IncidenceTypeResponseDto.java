package com.worktrace.worktracebackend.dto.incidenceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncidenceTypeResponseDto {
    private List<IncidenceTypeProjection> tipos;
}