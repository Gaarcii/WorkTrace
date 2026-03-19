package com.worktrace.worktracebackend.dto.timeEntry;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HistorialResponseDto {
    private Long minutosTrabajadosSemana;
    private Long minutosObjetivoSemana;
    private List<UltimosFichajesResponseDto> registrosDia;
    private Long minutosTrabajadosDia;
    private Long minutosObjetivoDia;

}
