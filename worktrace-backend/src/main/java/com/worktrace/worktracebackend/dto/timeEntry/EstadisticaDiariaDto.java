package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaDiariaDto {
    private LocalDate fecha;
    private long minutosTrabajados;
    private long minutosPrevistos;
}